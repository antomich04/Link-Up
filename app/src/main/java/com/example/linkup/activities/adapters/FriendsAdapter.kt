package com.example.linkup.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.Friendship
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.notifications.NotificationsHandler.Companion.FRIENDS_CHANNEL_ID
import com.example.linkup.activities.roomDB.Blocks
import com.example.linkup.activities.roomDB.UserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsAdapter(private val context: Context,
                     private var friendsList : List<Friendship>,
                     private val client : Client,
                     private val notificationHandler : NotificationsHandler,
                     private val userViewModel: UserViewModel,
                     private val loggedinUsername : String) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    var onFriendRemoved: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.friends_list_item, parent, false)

        //Sets item width to 70% of screen when in landscape mode
        val orientation = parent.context.resources.configuration.orientation
        if(orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE){
            val screenWidth = parent.context.resources.displayMetrics.widthPixels
            view.layoutParams.width = (screenWidth * 0.7).toInt()
        }else{
            view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendsList[position])
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateData(newFriendsList: List<Friendship>){
        friendsList = newFriendsList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view : View) : RecyclerView.ViewHolder(view){
        private val friendUsername = view.findViewById<TextView>(R.id.friendUsername)
        private val messageBtn = view.findViewById<FloatingActionButton>(R.id.messageBtn)
        private val blockUserBtn = view.findViewById<FloatingActionButton>(R.id.blockUserBtn)
        private val removeFriendBtn = view.findViewById<FloatingActionButton>(R.id.removeFriendBtn)

        fun bind(friendship: Friendship){
            val displayedUsername =
                if(friendship.userUsername!!.id == loggedinUsername){
                    friendship.friendUsername!!.id
                }else{
                    friendship.userUsername!!.id
                }
            friendUsername.text = displayedUsername



            messageBtn.setOnClickListener{
                //Add functionality
            }


            removeFriendBtn.setOnClickListener{
                MaterialAlertDialogBuilder(context)
                    .setTitle("Friend removal confirmation")
                    .setMessage("Are you sure you want to remove $displayedUsername from your friends list?")
                    .setNegativeButton("Cancel"){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Confirm"){ dialog, _ ->
                        client.removeFriend(loggedinUsername, displayedUsername){
                            notificationHandler.createNotificationChannel(FRIENDS_CHANNEL_ID, "Friends", "This channel provides information about the user's friends related actions")
                            notificationHandler.showRemovedFriendNotification(displayedUsername)

                            onFriendRemoved?.invoke()
                        }
                        dialog.dismiss()
                    }
                    .show()

            }

            blockUserBtn.setOnClickListener{
                MaterialAlertDialogBuilder(context)
                    .setTitle("Block user confirmation")
                    .setMessage("Are you sure you want to block $displayedUsername?")
                    .setNegativeButton("Cancel"){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Confirm"){ dialog, _ ->
                        client.blockUser(loggedinUsername, displayedUsername){
                            userViewModel.blockUser(Blocks(loggedinUsername, displayedUsername, System.currentTimeMillis()))
                            notificationHandler.createNotificationChannel(FRIENDS_CHANNEL_ID, "Friends", "This channel provides information about the user's friends related actions")
                            notificationHandler.showBlockedUserNotification(displayedUsername)
                        }
                        dialog.dismiss()
                    }
                    .show()

            }
        }
    }

}