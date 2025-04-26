package com.example.linkup.activities.fragments

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
import com.example.linkup.activities.notifications.NotificationsHandler.Companion.SETTINGS_CHANNEL_ID
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ChangePassword : Fragment() {
    private lateinit var backToOptionsBtn : FloatingActionButton
    private lateinit var newPasswordTxt : TextInputEditText
    private lateinit var newPasswordInputContainer : TextInputLayout
    private lateinit var changePasswordBtn : Button
    private lateinit var client : Client
    private lateinit var userViewModel: UserViewModel
    private var notificationsManager: NotificationManager? = null
    private lateinit var notificationsHandler: NotificationsHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.change_password, container, false)

        backToOptionsBtn = view.findViewById(R.id.backToOptionsBtn2)
        newPasswordTxt = view.findViewById(R.id.newPasswordInput)
        newPasswordInputContainer = view.findViewById(R.id.newPasswordInputContainer)
        changePasswordBtn = view.findViewById(R.id.changePasswordBtn)
        client = Client()

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        notificationsManager = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationsHandler = NotificationsHandler(requireContext())
        notificationsHandler.createNotificationChannel(SETTINGS_CHANNEL_ID, "Settings", "This channel provides information about the user's account related actions")

        backToOptionsBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        lifecycleScope.launch {
            val loggedinUser = userViewModel.getLoggedInUser()
            changePasswordBtn.setOnClickListener {
                val newPassword = newPasswordTxt.text.toString()
                if(newPassword.isEmpty()){
                    newPasswordInputContainer.error = getString(R.string.password_error)
                    return@setOnClickListener
                }else if(newPassword.length<6){
                    newPasswordInputContainer.error = getString(R.string.small_password_error)
                    return@setOnClickListener
                }else if(loggedinUser!!.password==newPassword){
                    newPasswordInputContainer.error = getString(R.string.invalid_password)
                    return@setOnClickListener
                }else{
                    newPasswordInputContainer.error = null
                }
                client.changePassword(loggedinUser.username, newPassword, onSuccess = {
                    userViewModel.changePassword(loggedinUser.username, newPassword)
                    requireActivity().runOnUiThread {
                        notificationsHandler.showChangedPasswordNotification()
                        parentFragmentManager.popBackStack()
                    }
                }, onFailure = {})
            }
        }

        view.setOnTouchListener { _, _ ->
            hideKeyboard(view)
            newPasswordTxt.clearFocus()
            false
        }

        return view
    }

    private fun hideKeyboard(view : View){
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}