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
        if(username!=null){
            client.saveToken(username, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.let{ data ->
            val title = data["title"]
            val body = data["body"]
            val target = data["target"]

            val intent = NotificationRouter.createIntent(applicationContext, target)
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(applicationContext, NotificationsHandler.REQUESTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.friends)
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