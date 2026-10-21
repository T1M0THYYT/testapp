package com.example.pixelsaver

import android.content.ComponentName
import android.service.notification.NotificationListenerService

class MediaNotificationListener : NotificationListenerService() {
    companion object {
        fun getComponentName(context: android.content.Context): ComponentName {
            return ComponentName(context, MediaNotificationListener::class.java)
        }
    }
}