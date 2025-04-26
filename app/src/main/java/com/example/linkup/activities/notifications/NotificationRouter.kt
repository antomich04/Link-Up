package com.example.linkup.activities.notifications

import android.content.Context
import android.content.Intent
import com.example.linkup.activities.HomePageActivity

//Used to help redirect the user to the home page when a push notification is clicked
object NotificationRouter {
    fun createIntent(context: Context): Intent {
        return Intent(context, HomePageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}