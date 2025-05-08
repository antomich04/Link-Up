package com.example.linkup.activities.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.adapters.MessagesAdapter
import com.example.linkup.activities.firestoreDB.Client
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ChatContainer : Fragment() {
    private lateinit var userUsername: String
    private lateinit var friendUsername: String
    private lateinit var client: Client
    private lateinit var rvMessages : RecyclerView
    private lateinit var messageTxt : TextInputEditText
    private lateinit var sendMessageBtn : FloatingActionButton
    private lateinit var messagesAdapter: MessagesAdapter
    private var lastOpenedTime: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.chat_container, container, false)

        arguments?.let{
            userUsername = it.getString("loggedInUser", "")
            friendUsername = it.getString("friendUser", "")
        }

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle(friendUsername)

        client = Client()

        messagesAdapter = MessagesAdapter(emptyList(), userUsername, client, requireContext())
        rvMessages = view.findViewById(R.id.rvMessages)
        rvMessages.layoutManager = LinearLayoutManager(requireContext())
        rvMessages.adapter = messagesAdapter

        client.getChatMessages(userUsername, friendUsername).observe(viewLifecycleOwner) { messages ->
            messagesAdapter.updateData(messages)
            rvMessages.scrollToPosition(messagesAdapter.itemCount-1)

            lastOpenedTime = System.currentTimeMillis()
            client.markMessagesAsSeen(userUsername, friendUsername, lastOpenedTime)
        }

        messageTxt = view.findViewById(R.id.messageTxt)
        sendMessageBtn = view.findViewById(R.id.sendMessageBtn)
        sendMessageBtn.setOnClickListener{
            if(!messageTxt.text!!.isEmpty()){
                client.sendMessage(userUsername, friendUsername, messageTxt.text.toString()){
                    messageTxt.setText("")
                }
            }
        }

        view.setOnTouchListener { _, _ ->
            hideKeyboard(view)
            messageTxt.clearFocus()
            rvMessages.clearFocus()
            false
        }

        return view
    }

    private fun hideKeyboard(view : View){
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}