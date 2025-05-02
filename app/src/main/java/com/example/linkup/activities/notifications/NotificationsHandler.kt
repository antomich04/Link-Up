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

    fun showChangedUsernameNotification(){
        val notification = NotificationCompat.Builder(context, SETTINGS_CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Successfull change")
            .setContentText("Your username has been changed!")
            .build()
        notificationManager.notify(2, notification)
    }

    fun showChangedPasswordNotification(){
        val notification = NotificationCompat.Builder(context, SETTINGS_CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Successfull change")
            .setContentText("Your password has been changed!")
            .build()
        notificationManager.notify(3, notification)
    }

    fun showRemovedFriendNotification(friend: String){
        val notification = NotificationCompat.Builder(context, FRIENDS_CHANNEL_ID)
            .setSmallIcon(R.drawable.friends)
            .setContentTitle("Friend removed")
            .setContentText("$friend has been removed from your friends list!")
            .build()
        notificationManager.notify(4, notification)
    }

    fun showDeletedAccountNotification(){
        val notification = NotificationCompat.Builder(context, SETTINGS_CHANNEL_ID)
            .setSmallIcon(R.drawable.settings)
            .setContentTitle("Account deleted")
            .setContentText("Your account has been deleted!")
            .build()
        notificationManager.notify(5, notification)
    }

    fun showBlockedUserNotification(blockedUser: String){
        val notification = NotificationCompat.Builder(context, FRIENDS_CHANNEL_ID)
            .setSmallIcon(R.drawable.friends)
            .setContentTitle("User blocked")
            .setContentText("$blockedUser has been blocked!")
            .build()
        notificationManager.notify(6, notification)
    }

    fun showUnblockedUserNotification(blockedUser: String){
        val notification = NotificationCompat.Builder(context, FRIENDS_CHANNEL_ID)
            .setSmallIcon(R.drawable.friends)
            .setContentTitle("User unblocked")
            .setContentText("$blockedUser has been unblocked!")
            .build()
        notificationManager.notify(7, notification)
    }

    fun createNotificationChannel(id: String, name: String, desc: String) {
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply{
            this.description = desc
        }

        notificationManager.createNotificationChannel(channel)
    }

    companion object{
        const val REQUESTS_CHANNEL_ID = "Requests"
        const val SETTINGS_CHANNEL_ID = "Settings"
        const val FRIENDS_CHANNEL_ID = "Friends"
    }
}