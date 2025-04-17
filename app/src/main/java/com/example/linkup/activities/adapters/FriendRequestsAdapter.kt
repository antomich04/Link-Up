package com.example.linkup.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.FriendRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendRequestsAdapter(private var friendRequests : List<FriendRequest>,
                            private val onReject: (String) -> Unit) : RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.friend_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendRequests[position], onReject)
    }

    override fun getItemCount(): Int {
        return friendRequests.size
    }

    fun updateData(newList: List<FriendRequest>) {
        friendRequests = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view : View) : RecyclerView.ViewHolder(view){
        private val usernameTxt = view.findViewById<TextView>(R.id.fromUsername)
        private val acceptBtn: FloatingActionButton = view.findViewById(R.id.acceptBtn)
        private val rejectBtn: FloatingActionButton = view.findViewById(R.id.rejectBtn)

        fun bind(friendRequest: FriendRequest, onReject: (String) -> Unit) {
            usernameTxt.text = friendRequest.sender!!.id

            acceptBtn.setOnClickListener {
                //client.acceptFriendRequest()
            }

            rejectBtn.setOnClickListener {
                onReject(friendRequest.id)
            }
        }
    }
}