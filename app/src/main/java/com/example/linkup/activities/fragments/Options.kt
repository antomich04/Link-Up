package com.example.linkup.activities.fragments

import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.StartingActivity
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlin.jvm.java

class Options : Fragment() {
    private lateinit var changeUsernameButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var userViewModel: UserViewModel
    private lateinit var client : Client
    private lateinit var notificationsManager: NotificationManager
    private lateinit var notificationsHandler: NotificationsHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.options, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("OPTIONS")

        changeUsernameButton = view.findViewById(R.id.changeUsernameBTn)
        changePasswordButton = view.findViewById(R.id.changePasswordBtn)
        deleteAccountButton = view.findViewById(R.id.deleteAccountBtn)

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)
        client = Client()

        notificationsManager = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationsHandler = NotificationsHandler(requireContext())

        changeUsernameButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, ChangeUsername()).addToBackStack(null).commit()
        }

        changePasswordButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, ChangePassword()).addToBackStack(null).commit()
        }

        deleteAccountButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete account confirmation")
                .setMessage("This will permanently delete all your data. Continue?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Confirm") { dialog, _ ->
                    lifecycleScope.launch{
                        val loggedInUser = userViewModel.getLoggedInUser()

                        client.deleteUser(loggedInUser!!.username, onSuccess = {
                            userViewModel.deleteUser(loggedInUser)
                            notificationsHandler.showDeletedAccountNotification()
                            val intent = Intent(requireActivity(), StartingActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        })
                    }
                }
                .show()
        }

        return view
    }
}