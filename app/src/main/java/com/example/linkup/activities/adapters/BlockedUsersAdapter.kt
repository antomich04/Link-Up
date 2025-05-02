package com.example.linkup.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.roomDB.Blocks
import com.example.linkup.activities.roomDB.UserViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockedUsersAdapter(
    private var blockedUsersList : List<Blocks>,
    private val client: Client,
    private val userViewModel: UserViewModel,
    private val notificationsHandler: NotificationsHandler
) : RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.blocked_users_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockedUsersList[position])
    }

    override fun getItemCount(): Int {
        return blockedUsersList.size
    }

    fun updateData(newList: List<Blocks>) {
        blockedUsersList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view : View) : RecyclerView.ViewHolder(view){
        private val blockedAtTxt : TextView = view.findViewById(R.id.blockedAtTxt)
        private val blockedUserUsername : TextView = view.findViewById(R.id.blockedUserUsername)
        private val unblockUserBtn : FloatingActionButton = view.findViewById(R.id.unblockUserBtn)

        fun bind(blocks: Blocks){

            blockedUserUsername.text = blocks.blockedUsername
            val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val formattedDate = formatter.format(Date(blocks.blockedAt))
            blockedAtTxt.text = "Blocked at: $formattedDate"

            unblockUserBtn.setOnClickListener{
                client.unblockUser(blocks.userUsername, blocks.blockedUsername){
                    userViewModel.unblockUser(blocks.userUsername, blocks.blockedUsername)
                    notificationsHandler.showUnblockedUserNotification(blocks.blockedUsername)
                }
            }
        }
    }
}