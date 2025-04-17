package com.example.linkup.activities.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AddFriend : Fragment(){
    private lateinit var backToFriendsBtn: FloatingActionButton
    private lateinit var sendRequestBtn: Button
    private lateinit var friendUsernameTxt: EditText
    private lateinit var wrongUsername: TextView
    private lateinit var client : Client
    private lateinit var userViewModel: UserViewModel

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
        friendUsernameTxt = view.findViewById(R.id.friendUsernameTxt)
        wrongUsername = view.findViewById(R.id.wrongUsername)
        client = Client()

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
                    wrongUsername.text = getString(R.string.username_error)
                    return@setOnClickListener
                }else if(loggedinUser!!.username==friendUsername){
                    wrongUsername.text = getString(R.string.invalid_username)
                    return@setOnClickListener
                }else{
                    wrongUsername.text = ""
                }

                client.checkIfUsernameExists(friendUsername, onSuccess = { exists ->
                    if(!exists){
                        requireActivity().runOnUiThread {
                            wrongUsername.text = getString(R.string.user_not_found)
                        }
                    }else{
                        client.sendFriendRequest(
                            loggedinUser.username,
                            friendUsername,
                            onSuccess = {
                                requireActivity().runOnUiThread {
                                    parentFragmentManager.popBackStack()
                                }
                            },
                            onFailure = { exception ->
                                requireActivity().runOnUiThread {
                                    wrongUsername.text = when(exception.message){
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
                        wrongUsername.text = getString(R.string.request_failed)
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