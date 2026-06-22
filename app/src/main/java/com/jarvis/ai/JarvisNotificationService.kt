package com.jarvis.ai

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class JarvisNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        val isMessagingApp = packageName.contains("whatsapp") ||
                packageName.contains("instagram") ||
                packageName.contains("messenger") ||
                packageName.contains("messages")

        if (isMessagingApp) {
            val extras = sbn.notification.extras
            val sender = extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"
            val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            if (message.isNotEmpty()) {
                val intent = Intent("JARVIS_NOTIFICATION_ACTION").apply {
                    putExtra("sender", sender)
                    putExtra("message", message)
                    putExtra("app", packageName)
                }
                sendBroadcast(intent)
            }
        }
    }
}
