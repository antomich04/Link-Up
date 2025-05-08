package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.adapters.BlockedUsersAdapter
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.notifications.NotificationsHandler
import com.example.linkup.activities.roomDB.Blocks
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import kotlinx.coroutines.launch

class BlockedUsers : Fragment() {
    private lateinit var rvBlockedUsers : RecyclerView
    private lateinit var userViewModel: UserViewModel
    private lateinit var client : Client
    private lateinit var notificationsHandler: NotificationsHandler
    private lateinit var blockedUsersAdapter: BlockedUsersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.blocked_users, container, false)

        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)
        client = Client()
        notificationsHandler = NotificationsHandler(requireContext())

        rvBlockedUsers = view.findViewById(R.id.rvBlockedUsers)
        rvBlockedUsers.layoutManager = LinearLayoutManager(requireContext())
        blockedUsersAdapter = BlockedUsersAdapter(emptyList(), client, userViewModel, notificationsHandler)
        rvBlockedUsers.adapter = blockedUsersAdapter

        lifecycleScope.launch {
            val loggedinUser = userViewModel.getLoggedInUser()

            val blockedUsersObserver = Observer<List<Blocks>> { blockedUsersList ->
                blockedUsersAdapter.updateData(blockedUsersList)

                val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
                toolbar?.title = "TOTAL BLOCKS: ${blockedUsersList.size}"
            }
            userViewModel.getBlockedUsersList(loggedinUser!!.username).observe(viewLifecycleOwner, blockedUsersObserver)
        }

        return view
    }
}