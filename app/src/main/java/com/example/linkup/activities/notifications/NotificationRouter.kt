package com.example.linkup.activities.notifications

import android.content.Context
import android.content.Intent
import com.example.linkup.activities.HomePageActivity

//Used to help redirect the user to the home page when a push notification is clicked
object NotificationRouter {
    fun createIntent(context: Context, target: String?): Intent {
        return Intent(context, HomePageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target", target)
        }
    }
}