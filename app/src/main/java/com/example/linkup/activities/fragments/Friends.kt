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
import com.example.linkup.activities.adapters.FriendsAdapter
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class Friends : Fragment() {
    private lateinit var addFriendBtn: FloatingActionButton
    private lateinit var rvFriendsList: RecyclerView
    private lateinit var client : Client
    private lateinit var adapter : FriendsAdapter
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friends, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("FRIENDS")

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(requireContext())
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)
        client = Client()

        rvFriendsList = view.findViewById(R.id.rvFriendsList)
        rvFriendsList.layoutManager = LinearLayoutManager(requireContext())
        adapter = FriendsAdapter(emptyList(), "")       //Created to avoid initialization conflicts
        rvFriendsList.adapter = adapter

        lifecycleScope.launch{
            val loggedinUser = userViewModel.getLoggedInUser()

            adapter = FriendsAdapter(emptyList(), loggedinUser!!.username)
            rvFriendsList.adapter = adapter //Replaces the initial adapter

            client.getFriendsList(loggedinUser.username).observe(viewLifecycleOwner) { friendsList ->
                adapter.updateData(friendsList)
            }
        }

        addFriendBtn = view.findViewById(R.id.addFriendBtn)
        addFriendBtn.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, AddFriend()).addToBackStack(null).commit()
        }

        return view
    }
}