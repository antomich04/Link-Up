package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.linkup.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Friends : Fragment() {
    private lateinit var addFriendBtn: FloatingActionButton
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friends, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("FRIENDS")

        addFriendBtn = view.findViewById(R.id.addFriendBtn)
        addFriendBtn.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, AddFriend()).addToBackStack(null).commit()
        }

        return view
    }
}