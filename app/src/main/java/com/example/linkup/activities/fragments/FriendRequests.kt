package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.adapters.FriendRequestsAdapter
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import kotlinx.coroutines.launch

class FriendRequests : Fragment() {
    private lateinit var rvFriendRequests: RecyclerView
    private lateinit var client : Client
    private lateinit var adapter : FriendRequestsAdapter
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        val view = inflater.inflate(R.layout.friend_requests, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("FRIEND REQUESTS")

        client = Client()

        rvFriendRequests = view?.findViewById(R.id.rvFriendRequests)!!
        rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        adapter = FriendRequestsAdapter(emptyList(),
            onAccept = { requestId ->
                client.acceptFriendRequest(requestId, onSuccess = {}, onFailure = {ex -> })
            },
            onReject = { requestId ->
                client.rejectFriendRequest(requestId, onSuccess = {}, onFailure = {ex -> })
            }
        )

        rvFriendRequests.adapter = adapter

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        lifecycleScope.launch{
            val loggedinUser = userViewModel.getLoggedInUser()

            client.getIncomingFriendRequests(loggedinUser!!.username).observe(viewLifecycleOwner) { requests ->
                adapter.updateData(requests)
            }
        }

        return view
    }
}