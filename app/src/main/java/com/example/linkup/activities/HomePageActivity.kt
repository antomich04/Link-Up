package com.example.linkup.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.content.edit
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.fragments.About
import com.example.linkup.activities.fragments.BlockedUsers
import com.example.linkup.activities.fragments.ChatContainer
import com.google.firebase.messaging.FirebaseMessaging

class HomePageActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var userViewModel: UserViewModel
    private lateinit var loggedinUsername : TextView
    private lateinit var client : Client

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
        client = Client()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drLayout)
        navView = findViewById(R.id.navView)

        //Sets up the drawer toggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //Displays the current logged in username in the navigation drawer header
        userViewModel.observeLoggedInUser().observe(this) { user ->
            if(user != null){
                //Gets the push notifications token after successful signup
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val token = task.result
                        client.saveToken(user.username, token)
                    }
                }

                val headerView = navView.getHeaderView(0)
                loggedinUsername = headerView.findViewById(R.id.loggedinUsername)
                loggedinUsername.text = user.username
            }
        }

        //Handles redirection from push notifications
        if(savedInstanceState == null){
            handleNotificationNavigation(intent)
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, HomePage()).commit()
        }


        navView.setCheckedItem(R.id.homePage)
        navView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.homePage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, HomePage(), "HomePage").commit()
                }
                R.id.friendsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, Friends(), "Friends").commit()
                }
                R.id.requestsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FriendRequests(), "FriendRequests").commit()
                }
                R.id.blockedUsersPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, BlockedUsers(), "BlockedUsers").commit()
                }
                R.id.optionsPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, Options(), "Options").commit()
                }
                R.id.aboutPage -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, About(), "About").commit()
                }
                R.id.logoutBtn -> {
                    lifecycleScope.launch{
                        val loggedInUser = userViewModel.getLoggedInUser()
                        if(loggedInUser!=null){
                            userViewModel.logOutUser(loggedInUser.username)
                        }

                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            client.removeToken(loggedInUser!!.username, token)
                        }
                    }

                    //Removes the username to avoid push notifications token conflict
                    val sharedPref = getSharedPreferences("userPrefs", MODE_PRIVATE)
                    sharedPref.edit{
                        remove("loggedInUsername")
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationNavigation(intent)
    }


    private fun handleNotificationNavigation(intent: Intent?) {
        val targetFragment = intent?.getStringExtra("target") ?: return

        Handler(Looper.getMainLooper()).postDelayed({
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

            when(targetFragment){
                "FriendRequests" -> {
                    if(currentFragment !is FriendRequests){
                        navView.setCheckedItem(R.id.requestsPage)
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FriendRequests(), "FriendRequests").commit()
                    }
                }
                "Friends" -> {
                    if(currentFragment !is Friends){
                        navView.setCheckedItem(R.id.friendsPage)
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, Friends(), "Friends").commit()
                    }
                }
                "Chat" -> {
                    if(currentFragment !is ChatContainer){
                        navView.setCheckedItem(R.id.homePage)
                        val chatFragment = ChatContainer().apply {
                            arguments = Bundle().apply {
                                putString("loggedInUser", intent.getStringExtra("receiver"))
                                putString("friendUser", intent.getStringExtra("sender"))
                            }
                        }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, chatFragment, "ChatContainer")
                            .commit()
                    }else{
                        //Already in the chat fragment, updates the arguments if needed
                        currentFragment.arguments = Bundle().apply {
                            putString("loggedInUser", intent.getStringExtra("receiver"))
                            putString("friendUser", intent.getStringExtra("sender"))
                        }
                    }
                }
            }
        }, 300)
    }
}