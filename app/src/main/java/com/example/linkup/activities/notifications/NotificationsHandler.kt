package com.example.linkup.activities.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.linkup.R

class NotificationsHandler(private val context : Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showRequestsNotification(friend: String){
        val notification = NotificationCompat.Builder(context, REQUESTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.friends)
            .setContentTitle("Successful request")
            .setContentText("Your friend request to $friend has been sent!")
            .build()
        notificationManager.notify(1, notification)
    }

    fun createNotificationChannel(id: String, name: String, desc: String) {
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply{
            this.description = desc
        }

        notificationManager.createNotificationChannel(channel)
    }

    companion object{
        const val REQUESTS_CHANNEL_ID = "Requests"
    }
}