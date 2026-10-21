package com.example.pixelsaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }

        val btnAccess = Button(this).apply {
            text = "1. Grant Notification & Media Access"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val btnSaver = Button(this).apply {
            text = "2. Choose Pixel Saver as Screen Saver"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
            }
        }

        layout.addView(btnAccess)
        layout.addView(btnSaver)
        setContentView(layout)
    }
}