package com.example.linkup.activities.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Chat
import com.example.linkup.activities.fragments.ChatContainer
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale

class ChatsAdapter(
    private var chatsList : List<Chat>,
    private val loggedinUsername: String,
    private val fragmentManager: FragmentManager): RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chatsList[position])
    }

    override fun getItemCount(): Int {
        return chatsList.size
    }

    fun updateData(newList: List<Chat>){
        chatsList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view){
        private val senderUsername = view.findViewById<TextView>(R.id.senderUsername)
        private val lastMessage = view.findViewById<TextView>(R.id.lastMessageTxt)
        private val timestamp = view.findViewById<TextView>(R.id.timestampTxt)
        private val cardView = view.findViewById<MaterialCardView>(R.id.cardView)

        fun bind(chat: Chat){
            senderUsername.text = if(chat.sender == loggedinUsername) chat.receiver else chat.sender
            lastMessage.text = chat.lastMessage ?: ""

            try {
                val formattedDate = if(chat.timestamp != null){
                    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    formatter.format(chat.timestamp!!.toDate())
                }else{
                    "Sending..."
                }
                timestamp.text = formattedDate
            }catch(e: Exception){
                timestamp.text = "Sending..."
            }

            cardView.setOnClickListener{
                val chatFragment = ChatContainer()

                //Used to store the usernames for the new fragment
                val bundle = Bundle().apply {
                    putString("loggedInUser", loggedinUsername)
                    putString("friendUser", senderUsername.text.toString())
                }
                chatFragment.arguments = bundle

                fragmentManager.beginTransaction().replace(R.id.fragmentContainer, chatFragment).addToBackStack(null).commit()
            }
        }
    }
}