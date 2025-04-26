package com.example.linkup.activities.roomDB

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    //Marks user as logged out
    @Query("UPDATE User_Preferences SET isLoggedIn = false WHERE username = :username")
    suspend fun logOutUser(username: String)

    //Marks the user as logged in
    @Query("UPDATE User_Preferences SET isLoggedIn = true WHERE username = :username")
    suspend fun logInUser(username: String)

    @Query("SELECT * FROM Users WHERE username = :username")
    suspend fun getUserByUsername(username: String): LocalUser?

    @Insert
    suspend fun insertUser(user: LocalUser)

    @Insert
    suspend fun insertUserPreferences(userPreferences: UserPreferences)

    @Delete
    suspend fun deleteUser(user: LocalUser)

    @Query("SELECT * FROM Users WHERE username IN (SELECT username FROM User_Preferences WHERE isLoggedIn = 1) LIMIT 1")
    suspend fun getLoggedInUser(): LocalUser?

    //Used to dynamically change the username in the home page drawer if it changes
    @Query("SELECT * FROM Users WHERE username IN (SELECT username FROM User_Preferences WHERE isLoggedIn = 1) LIMIT 1")
    fun observeLoggedInUser(): LiveData<LocalUser?>

    @Query("UPDATE Users SET username = :newUsername WHERE username = :oldUsername")
    suspend fun changeUsername(newUsername: String, oldUsername: String)

    @Query("UPDATE Users SET password = :newPassword WHERE username = :username")
    suspend fun changePassword(username: String, newPassword: String)

}