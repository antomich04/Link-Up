package com.example.linkup.activities.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.Friendship
import com.example.linkup.activities.fragments.ChatContainer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView

class BottomSheetFriendsAdapter(
    private var friendsList : List<Friendship>,
    private val loggedInUsername: String,
    private val bottomSheetFriends: BottomSheetDialog,
    private val fragmentManager: FragmentManager,
    private val client: Client
): RecyclerView.Adapter<BottomSheetFriendsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bottom_sheet_friend_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int){
        holder.bind(friendsList[position])
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateData(newFriends: List<Friendship>) {
        friendsList = newFriends
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        private var friendName = view.findViewById<TextView>(R.id.bsFriendUsername)
        private var cvFriend = view.findViewById<MaterialCardView>(R.id.cvFriend)

        fun bind(friendship: Friendship){
            friendName.text = if(friendship.friendUsername!!.id == loggedInUsername){
                friendship.userUsername!!.id
            }else{
                friendship.friendUsername!!.id
            }

            cvFriend.setOnClickListener {
                val friendId = friendName.text.toString()

                //Creates chat
                client.createChat(loggedInUsername, friendId)

                val chatFragment = ChatContainer().apply{
                    arguments = Bundle().apply{
                        putString("loggedInUser", loggedInUsername)
                        putString("friendUser", friendId)
                    }
                }

                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, chatFragment)
                    .addToBackStack(null)
                    .commit()

                bottomSheetFriends.dismiss()
            }
        }
    }
}