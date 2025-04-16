package com.example.linkup.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.launch

class StartingActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.starting_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,systemBars.right, systemBars.bottom)
            insets
        }

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(applicationContext)
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        lifecycleScope.launch{
            val user = userViewModel.getLoggedInUser()
            if(user!=null){
                startActivity(Intent(this@StartingActivity, HomePageActivity::class.java))
                finish()
            }
        }

        val loginButton : Button = findViewById(R.id.loginButton)
        val signupButton : Button = findViewById(R.id.signupButton)
        loginButton.setOnClickListener{
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupButton.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}