package com.example.linkup.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.LoginException
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.LocalUser
import com.example.linkup.activities.roomDB.UserPreferences
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class LogInActivity : AppCompatActivity() {
    private lateinit var usernameInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var loginButton : Button
    private lateinit var backButton : FloatingActionButton
    private lateinit var rootLayout : ConstraintLayout
    private lateinit var usernameError : TextView
    private lateinit var passwordError : TextView
    private lateinit var userNotFoundError : TextView
    private val client = Client()
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.login_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(applicationContext)
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.submitSignupBtn)
        backButton = findViewById(R.id.backBtn)
        usernameError = findViewById(R.id.usernameError1)
        passwordError = findViewById(R.id.passwordError1)
        userNotFoundError = findViewById(R.id.userNotFoundError)
        rootLayout = findViewById(R.id.main) //Root view

        backButton.setOnClickListener{
            val intent = Intent(this, StartingActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener{
            if(validateLogin()){
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()
                client.loginUser(username, password, onSuccess ={
                    //Gets user name and email from Firestore
                    client.getUserCredentials(username, onSuccess = { name, email ->
                        lifecycleScope.launch {
                            val existingUser = userViewModel.getUserByUsername(username)
                            if(existingUser==null){
                                //Registers the user locally with fetched name and email
                                val newUser = LocalUser(
                                    username = username,
                                    password = password,
                                    email = email,
                                    name = name
                                )
                                val preferences = UserPreferences(
                                    username = username,
                                    isLoggedIn = true,
                                    appLanguage = "en"
                                )
                                userViewModel.registerUser(newUser, preferences)
                            }else{
                                //Marks as logged in
                                userViewModel.logInUser(username)
                            }

                            //Redirects to home page
                            startActivity(Intent(this@LogInActivity, HomePageActivity::class.java))
                            finish()
                        }
                    }, onFailure = {})
                }, onFailure = { exception ->
                    if(exception is LoginException){
                        when(exception.message){
                            "Incorrect password!" -> passwordError.text = getString(R.string.wrong_password)
                            "User does not exist!" -> userNotFoundError.text = getString(R.string.user_not_found)
                        }
                    }
                })
            }
        }


        //Sets up touch listener to hide keyboard when tapping outside EditText
        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard()
            usernameInput.clearFocus()
            passwordInput.clearFocus()
            false
        }
    }

    private fun hideKeyboard(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun validateLogin() : Boolean{
        if(usernameInput.text.isEmpty()){
            usernameError.text = getString(R.string.username_error)
        }else{
            usernameError.text = ""
        }

        if(passwordInput.text.isEmpty()){
            passwordError.text = getString(R.string.password_error)
        }else{
            passwordError.text = ""
        }

        userNotFoundError.text = ""
        val returnValue = usernameError.text.isEmpty() && passwordError.text.isEmpty()
        return returnValue
    }
}