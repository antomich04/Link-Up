package com.example.linkup.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Friendship
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendsAdapter(private var friendsList : List<Friendship>,
                     private val loggedinUsername : String) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.friends_list_item, parent, false)
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
                //Add functionality
            }
        }
    }

}