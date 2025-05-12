package com.example.linkup.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.Message
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Locale

class MessagesAdapter(
    private var messages: List<Message>,
    private val currentUser: String,
    private val client: Client,
    private val context: Context
): RecyclerView.Adapter<MessagesAdapter.ViewHolder>(){
    //Used to select which view to inflate based on the sender
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == currentUser) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_SENT)
            R.layout.message_item_sent
        else
            R.layout.message_item_received

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateData(newList: List<Message>){
        messages = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View, private val viewType: Int): RecyclerView.ViewHolder(itemView){
        //Selects the correct TextView based on the viewType (sent or received)
        private val messageText: TextView = if (viewType == VIEW_TYPE_SENT){
            itemView.findViewById(R.id.messageText)  //For sent messages
        }else{
            itemView.findViewById(R.id.messageText2)  //For received messages
        }

        private val messageTime: TextView = if (viewType == VIEW_TYPE_SENT){
            itemView.findViewById(R.id.messageTime)  //For sent messages
        }else{
            itemView.findViewById(R.id.messageTime2)  //For received messages
        }

        private val seenTxt: TextView? = if (viewType == VIEW_TYPE_SENT) {
            itemView.findViewById(R.id.seenTxt)
        }else null


        private val deleteMessageBtn: FloatingActionButton? = itemView.findViewById(R.id.deleteMessageBtn)

        fun bind(message: Message){
            messageText.text = message.text
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedDate = message.timestamp?.toDate()?.let {
                formatter.format(it)
            } ?: "Sending..."
            messageTime.text = formattedDate

            if(seenTxt!=null){
                if(message.seen && viewType == VIEW_TYPE_SENT){
                    seenTxt.visibility = View.VISIBLE
                    seenTxt.text = itemView.context.getString(R.string.seen)
                }else{
                    seenTxt.visibility = View.GONE
                }
            }

            deleteMessageBtn?.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete message confirmation")
                    .setMessage("Are you sure you want to delete this message?")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Confirm") { dialog, _ ->
                        val chatId = client.getChatId(message.sender, message.receiver)
                        client.deleteMessage(chatId, message.id) {
                            dialog.dismiss()
                        }
                    }
                    .show()
            }

        }
    }

}