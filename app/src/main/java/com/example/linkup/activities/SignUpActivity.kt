package com.example.linkup.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging

class SignUpActivity : AppCompatActivity() {
    private lateinit var nameInput : TextInputEditText
    private lateinit var usernameInput : TextInputEditText
    private lateinit var passwordInput : TextInputEditText
    private lateinit var nameInputContainer : TextInputLayout
    private lateinit var usernameInputContainer : TextInputLayout
    private lateinit var passwordInputContainer : TextInputLayout
    private lateinit var signupButton : Button
    private lateinit var backButton : FloatingActionButton
    private lateinit var rootLayout : ConstraintLayout
    private val client = Client()
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?){
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
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        nameInputContainer = findViewById(R.id.nameInputContainer)
        usernameInputContainer = findViewById(R.id.usernameInputContainer)
        passwordInputContainer = findViewById(R.id.passwordInputContainer)
        signupButton = findViewById(R.id.submitLoginBtn)
        backButton = findViewById(R.id.backBtn)
        rootLayout = findViewById(R.id.main)

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
                        usernameInputContainer.error = resources.getString(R.string.username_taken)
                    }else{
                        //Creates a new user
                        val newUser = User(
                            name = nameInput.text.toString(),
                            username = username,
                            password = passwordInput.text.toString()
                        )

                        //Creates a new user locally
                        val localUser = LocalUser(
                            name = nameInput.text.toString(),
                            username = username,
                            password = passwordInput.text.toString()
                        )

                        val preferences = UserPreferences(
                            username = username,
                            isLoggedIn = true
                        )

                        //Registers the user locally
                        userViewModel.registerUser(localUser, preferences)

                        //Inserts user into Firestore
                        client.insertUser(newUser, onSuccess = {

                            //Gets the push notifications token after successful signup
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    client.saveToken(username, token)
                                }
                            }

                            val intent = Intent(this, HomePageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, onFailure = {})
                    }
                }, onFailure = {})
            }
        }

        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard()
            nameInput.clearFocus()
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
        if(nameInput.text!!.isEmpty()){
            nameInputContainer.error = getString(R.string.name_error)
        }else{
            nameInputContainer.error = null
        }

        if(usernameInput.text!!.isEmpty()){
            usernameInputContainer.error = getString(R.string.username_error)
        }else{
            usernameInputContainer.error = null
        }

        if(passwordInput.text!!.isEmpty()){
            passwordInputContainer.error = getString(R.string.password_error)
        }else if(passwordInput.text!!.length < 6){
            passwordInputContainer.error = getString(R.string.small_password_error)
        }else{
            passwordInputContainer.error = null
        }

        val returnValue = nameInputContainer.error.isNullOrEmpty() && usernameInputContainer.error.isNullOrEmpty() && passwordInputContainer.error.isNullOrEmpty()
        return returnValue
    }
}