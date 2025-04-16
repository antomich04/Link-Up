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
import com.example.linkup.R
import com.example.linkup.activities.firestoreDB.Client
import com.example.linkup.activities.firestoreDB.User
import com.example.linkup.activities.roomDB.LocalDatabase
import com.example.linkup.activities.roomDB.LocalUser
import com.example.linkup.activities.roomDB.UserPreferences
import com.example.linkup.activities.roomDB.UserViewModel
import com.example.linkup.activities.roomDB.UserViewModelFactory
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SignUpActivity : AppCompatActivity() {
    private lateinit var nameInput : EditText
    private lateinit var emailInput : EditText
    private lateinit var usernameInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var signupButton : Button
    private lateinit var backButton : FloatingActionButton
    private lateinit var rootLayout : ConstraintLayout
    private lateinit var nameError : TextView
    private lateinit var emailError : TextView
    private lateinit var usernameError : TextView
    private lateinit var passwordError : TextView
    private val client = Client()
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.signup_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.email)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        signupButton = findViewById(R.id.submitSignupBtn)
        backButton = findViewById(R.id.backBtn)
        rootLayout = findViewById(R.id.main)
        nameError = findViewById(R.id.nameError)
        emailError = findViewById(R.id.emailError)
        usernameError = findViewById(R.id.usernameError2)
        passwordError = findViewById(R.id.passwordError2)

        //Initializes Room Database + ViewModel
        val database = LocalDatabase.getDB(applicationContext)
        val userDao = database.userDao()
        val factory = UserViewModelFactory(userDao)
        userViewModel = factory.create(UserViewModel::class.java)

        backButton.setOnClickListener {
            val intent = Intent(this, StartingActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupButton.setOnClickListener {
            if(validateSignup()){
                val username = usernameInput.text.toString()

                //Checks if the username already exists
                client.checkIfUsernameExists(username, onSuccess = { exists ->
                    if(exists){
                        usernameError.text = resources.getString(R.string.username_taken)
                    }else{
                        //Creates a new user
                        val newUser = User(
                            name = nameInput.text.toString(),
                            email = emailInput.text.toString(),
                            username = username,
                            password = passwordInput.text.toString()
                        )

                        //Creates a new user locally
                        val localUser = LocalUser(
                            name = nameInput.text.toString(),
                            email = emailInput.text.toString(),
                            username = username,
                            password = passwordInput.text.toString()
                        )

                        val preferences = UserPreferences(
                            username = username,
                            isLoggedIn = true,
                            appLanguage = "en"
                        )

                        //Registers the user locally
                        userViewModel.registerUser(localUser, preferences)

                        //Inserts user into Firestore
                        client.insertUser(newUser, onSuccess = {
                            val intent = Intent(this, HomePageActivity::class.java)
                            startActivity(intent)
                        }, onFailure = {})
                    }
                }, onFailure = {})
            }
        }

        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard()
            nameInput.clearFocus()
            emailInput.clearFocus()
            usernameInput.clearFocus()
            passwordInput.clearFocus()
            false
        }
    }

    private fun hideKeyboard(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let{
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun validateSignup() : Boolean{
        if(nameInput.text.isEmpty()){
            nameError.text = getString(R.string.name_error)
        }else{
            nameError.text = ""
        }

        if(emailInput.text.isEmpty()){
            emailError.text = getString(R.string.email_error)
        }else{
            emailError.text = ""
        }

        if(usernameInput.text.isEmpty()){
            usernameError.text = getString(R.string.username_error)
        }else{
            usernameError.text = ""
        }

        if(passwordInput.text.isEmpty()){
            passwordError.text = getString(R.string.password_error)
        }else if(passwordInput.text.length < 6){
            passwordError.text = getString(R.string.small_password_error)
        }else{
            passwordError.text = ""
        }

        val returnValue = nameError.text.isEmpty() && emailError.text.isEmpty() && usernameError.text.isEmpty() && passwordError.text.isEmpty()
        return returnValue
    }
}