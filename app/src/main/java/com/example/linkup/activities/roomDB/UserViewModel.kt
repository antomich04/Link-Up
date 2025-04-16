package com.example.linkup.activities.roomDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {

    //Inserts a new user and their preferences
    fun registerUser(user: LocalUser, preferences: UserPreferences) = viewModelScope.launch(Dispatchers.IO) {
        userDao.insertUser(user)
        userDao.insertUserPreferences(preferences)
    }

    //Marks a user as logged in
    fun logInUser(username: String) = viewModelScope.launch(Dispatchers.IO) {
        userDao.logInUser(username)
    }

    //Gets a user by their username
    suspend fun getUserByUsername(username: String): LocalUser? {
        return userDao.getUserByUsername(username)
    }

    //Marks a user as logged out
    fun logOutUser(username: String) = viewModelScope.launch(Dispatchers.IO) {
        userDao.logOutUser(username)
    }

    //Deletes a user and their preferences
    fun deleteUser(user: LocalUser) = viewModelScope.launch(Dispatchers.IO) {
        userDao.deleteUser(user)
    }

    //Gets the currently logged in user
    suspend fun getLoggedInUser(): LocalUser? {
        return userDao.getLoggedInUser()
    }
}
