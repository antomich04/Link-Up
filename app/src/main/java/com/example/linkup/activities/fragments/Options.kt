package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.linkup.R

class Options : Fragment() {
    private lateinit var changeUsernameButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.options, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("OPTIONS")

        changeUsernameButton = view.findViewById(R.id.changeUsernameBTn)
        changePasswordButton = view.findViewById(R.id.changePasswordBtn)
        deleteAccountButton = view.findViewById(R.id.deleteAccountBtn)

        changeUsernameButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, ChangeUsername()).addToBackStack(null).commit()
        }

        changePasswordButton.setOnClickListener{
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer, ChangePassword()).addToBackStack(null).commit()
        }

        deleteAccountButton.setOnClickListener{
            //TODO: Delete account
        }

        return view
    }
}