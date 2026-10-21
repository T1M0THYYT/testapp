package com.example.pixelsaver

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.dreams.DreamService
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class PixelMusicDreamService : DreamService() {

    private lateinit var clockContainer: View
    private lateinit var musicContainer: View
    private lateinit var ivAlbumArt: ImageView
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPlayPause: Button

    private var sessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null

    // Callbacks to monitor playback changes
    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateUI()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateUI()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        findAndAttachActiveController(controllers)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Screen saver configuration
        isInteractive = true          // Allows tapping the media buttons
        isFullscreen = true
        isScreenBright = true

        setContentView(R.layout.dream_layout)

        clockContainer = findViewById(R.id.clockContainer)
        musicContainer = findViewById(R.id.musicContainer)
        ivAlbumArt = findViewById(R.id.ivAlbumArt)
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtist = findViewById(R.id.tvArtist)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            activeController?.transportControls?.skipToPrevious()
        }
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            activeController?.transportControls?.skipToNext()
        }
        btnPlayPause.setOnClickListener {
            val state = activeController?.playbackState?.state
            if (state == PlaybackState.STATE_PLAYING) {
                activeController?.transportControls?.pause()
            } else {
                activeController?.transportControls?.play()
            }
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        setupMediaListener()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        cleanupMediaListener()
    }

    private fun setupMediaListener() {
        sessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            val component = MediaNotificationListener.getComponentName(this)
            sessionManager?.addOnActiveSessionsChangedListener(sessionsChangedListener, component)
            val initialControllers = sessionManager?.getActiveSessions(component)
            findAndAttachActiveController(initialControllers)
        } catch (e: SecurityException) {
            // Notification Listener access is missing
            showClock()
        }
    }

    private fun cleanupMediaListener() {
        activeController?.unregisterCallback(controllerCallback)
        sessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
    }

    private fun findAndAttachActiveController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(controllerCallback)
        
        // Find session that is currently playing or paused (most recent)
        activeController = controllers?.firstOrNull { controller ->
            val state = controller.playbackState?.state
            state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
        } ?: controllers?.firstOrNull()

        activeController?.registerCallback(controllerCallback)
        updateUI()
    }

    private fun updateUI() {
        val state = activeController?.playbackState?.state
        val isMusicPlaying = (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING)

        if (isMusicPlaying && activeController != null) {
            val metadata = activeController?.metadata
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Track"
            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val art: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

            tvSongTitle.text = title
            tvArtist.text = artist
            if (art != null) {
                ivAlbumArt.setImageBitmap(art)
            } else {
                ivAlbumArt.setImageResource(android.R.drawable.ic_media_play)
            }

            btnPlayPause.text = if (state == PlaybackState.STATE_PLAYING) "⏸" else "▶"

            showMusicPlayer()
        } else {
            showClock()
        }
    }

    private fun showClock() {
        clockContainer.visibility = View.VISIBLE
        musicContainer.visibility = View.GONE
    }

    private fun showMusicPlayer() {
        clockContainer.visibility = View.GONE
        musicContainer.visibility = View.VISIBLE
    }
}