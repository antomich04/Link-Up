package com.example.linkup.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.linkup.R

class About : Fragment() {
    private lateinit var aboutTxt: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.about_page, container, false)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setTitle("ABOUT")

        aboutTxt = view.findViewById(R.id.aboutTxt)
        aboutTxt.text = """
    Link Up is a simple chatting application that was developed as part of the course "Advanced Human-Machine Interaction Topics" during the sixth semester at International Hellenic University. 
            
    The author of this program is Antonios Michailos, who was born on July 12th, 2004 in Katerini, Greece. This application makes use of Room API as well as Firebase's Firestore in order to store data both locally and remotely. 
            
    Apart from these, Firebase's functions are used to implement push notifications logic where the user can receive a notification even when the application is not running. 
            
    Regarding the application's UI, it is fully compliant with Material 3 and it supports the dynamic colors feature where the colors of the application change accordingly to the user's selected system colors.
        """.trimEnd()

        return view
    }
}