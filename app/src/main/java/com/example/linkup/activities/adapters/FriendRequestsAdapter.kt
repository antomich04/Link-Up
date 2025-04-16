package com.example.linkup.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.FriendRequest

class FriendRequestsAdapter(val friendRequests : List<FriendRequest>) : RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.friend_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        holder.bind(friendRequests[position])
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    inner class ViewHolder(private val view : View) : RecyclerView.ViewHolder(view){
        fun bind(friendRequest: FriendRequest){
            val usernameTxt = view.findViewById<TextView>(R.id.fromUsername)
            usernameTxt.text = friendRequest.sender.toString()
        }
    }
}