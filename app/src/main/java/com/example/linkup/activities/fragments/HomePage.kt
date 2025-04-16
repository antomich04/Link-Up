package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.linkup.R

class HomePage : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("CHATS")
        return inflater.inflate(R.layout.home_page, container, false)
    }
}