package com.example.linkup.activities.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.notifications.NotificationsHandler.Companion.REQUESTS_CHANNEL_ID
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AddFriend : Fragment(){
    private lateinit var backToFriendsBtn: FloatingActionButton
    private lateinit var sendRequestBtn: Button
    private lateinit var friendUsernameTxt: TextInputEditText
    private lateinit var usernameInputContainer: TextInputLayout
    private lateinit var client : Client
    private lateinit var userViewModel: UserViewModel
    private var notificationsManager: NotificationManager? = null
    private lateinit var notificationsHandler: NotificationsHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_friend, container, false)

        backToFriendsBtn = view.findViewById(R.id.backToFriendsBtn)
        sendRequestBtn = view.findViewById(R.id.sendRequestBtn)
        friendUsernameTxt = view.findViewById(R.id.friendUsernameInput)
        usernameInputContainer = view.findViewById(R.id.usernameInputContainer)
        client = Client()

        notificationsManager = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationsHandler = NotificationsHandler(requireContext())
        notificationsHandler.createNotificationChannel(REQUESTS_CHANNEL_ID, "Requests", "This channel provides information about the user's friend requests")

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        backToFriendsBtn.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        lifecycleScope.launch {
            val loggedinUser = userViewModel.getLoggedInUser()

            sendRequestBtn.setOnClickListener {
                val friendUsername = friendUsernameTxt.text.toString()
                if(friendUsername.isEmpty()){
                    usernameInputContainer.error = getString(R.string.username_error)
                    return@setOnClickListener
                }else if(loggedinUser!!.username==friendUsername){
                    usernameInputContainer.error = getString(R.string.invalid_username)
                    return@setOnClickListener
                }else{
                    usernameInputContainer.error = null
                }

                client.checkIfUsernameExists(friendUsername, onSuccess = { exists ->
                    if(!exists){
                        requireActivity().runOnUiThread {
                            usernameInputContainer.error = getString(R.string.user_not_found)
                        }
                    }else{
                        client.sendFriendRequest(
                            loggedinUser.username,
                            friendUsername,
                            onSuccess = {
                                requireActivity().runOnUiThread {
                                    notificationsHandler.showRequestsNotification(friendUsername)
                                    parentFragmentManager.popBackStack()
                                }
                            },
                            onFailure = { exception ->
                                requireActivity().runOnUiThread {
                                    usernameInputContainer.error = when(exception.message){
                                        "Friend request already sent or already friends" ->
                                            getString(R.string.request_already_sent)
                                        else ->
                                            getString(R.string.request_failed)
                                    }
                                }
                            }
                        )
                    }
                }, onFailure = {
                    requireActivity().runOnUiThread {
                        usernameInputContainer.error = getString(R.string.request_failed)
                    }
                })
            }
        }

        view.setOnTouchListener { _, _ ->
            hideKeyboard(view)
            friendUsernameTxt.clearFocus()
            false
        }

        return view
    }

    private fun hideKeyboard(view : View){
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}