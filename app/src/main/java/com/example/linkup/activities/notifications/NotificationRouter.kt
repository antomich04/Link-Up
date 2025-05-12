package com.example.linkup.activities.notifications

import android.content.Context
import android.content.Intent
import com.example.linkup.activities.HomePageActivity

object NotificationRouter {
    fun createIntent(context: Context, target: String?, sender: String? = null, receiver: String? = null): Intent {
        return Intent(context, HomePageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target", target)
            putExtra("sender", sender)
            putExtra("receiver", receiver)
        }
    }
}