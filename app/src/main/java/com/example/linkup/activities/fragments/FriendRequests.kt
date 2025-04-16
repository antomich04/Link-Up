package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkup.R
import com.example.linkup.activities.adapters.FriendRequestsAdapter
import com.example.linkup.activities.firestoreDB.FriendRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton

//TODO: NA FTIAXW TO PROVLIMA OPOU TA INTERFACES DEN PIANOUN OLOKLIRI TIN OTHONI KAI NA EMFANISO TA FRIEND REQUESTS

class FriendRequests : Fragment() {
    private lateinit var rvFriendRequests: RecyclerView
    private lateinit var acceptBtn : FloatingActionButton
    private lateinit var rejectBtn : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{
        val view = inflater.inflate(R.layout.friend_requests, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("FRIEND REQUESTS")

        rvFriendRequests = view?.findViewById(R.id.rvFriendRequests)!!
        rvFriendRequests.layoutManager = LinearLayoutManager(requireContext())
        rvFriendRequests.adapter = FriendRequestsAdapter(listOf())

        acceptBtn = rvFriendRequests.findViewById(R.id.acceptBtn)
        rejectBtn = rvFriendRequests.findViewById(R.id.rejectBtn)

        return view
    }
}