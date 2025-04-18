package com.example.linkup.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.fragments.FriendRequests
import com.example.linkup.activities.fragments.Friends
import com.example.linkup.activities.fragments.HomePage
import com.example.linkup.activities.fragments.Options
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var userViewModel: UserViewModel
    private lateinit var loggedinUsername : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.home_page_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(applicationContext)
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drLayout)
        navView = findViewById(R.id.navView)

        //Sets up the drawer toggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Displays the current logged in username in the navigation drawer header
        lifecycleScope.launch {
            val loggedInUser = userViewModel.getLoggedInUser()
            if (loggedInUser != null) {
                val headerView = navView.getHeaderView(0)
                loggedinUsername = headerView.findViewById(R.id.loggedinUsername)
                loggedinUsername.text = loggedInUser.username
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, HomePage()).commit()
        }


        navView.setCheckedItem(R.id.homePage)
        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.homePage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, HomePage()).commit()
                }
                R.id.friendsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, Friends()).commit()
                }
                R.id.requestsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FriendRequests()).commit()
                }
                R.id.optionsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, Options()).commit()
                }
                R.id.logoutBtn -> {
                    lifecycleScope.launch{
                        val loggedInUser = userViewModel.getLoggedInUser()
                        if(loggedInUser!=null){
                            userViewModel.logOutUser(loggedInUser.username)
                        }
                    }
                    val intent = Intent(this, StartingActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            navView.setCheckedItem(menuItem.itemId)
            drawerLayout.closeDrawers()
            true
        }
    }

    //Required to pass drawer events to the toggle
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(toggle.onOptionsItemSelected(item)){
            true
        }else{
            super.onOptionsItemSelected(item)
        }
    }
}