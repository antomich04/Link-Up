package com.example.linkup.activities.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    private lateinit var client: Client

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        client = Client()
        val sharedPref = getSharedPreferences("userPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUsername", null)
        if(username != null){
            client.saveToken(username, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.let { data ->
            val title = data["title"]
            val body = data["body"]
            val target = data["target"]
            val sender = data["sender"]
            val receiver = data["receiver"]

            val intent = NotificationRouter.createIntent(applicationContext, target, sender, receiver)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = when(target){
                "Chat" -> NotificationsHandler.MESSAGES_CHANNEL_ID
                else -> NotificationsHandler.REQUESTS_CHANNEL_ID
            }

            val smallIcon = when(target){
                "Chat" -> R.drawable.new_message
                else -> R.drawable.friends
            }

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(10, notification)
        }
    }
}