package com.example.linkup.activities.roomDB

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserDao_Impl(
  __db: RoomDatabase,
) : UserDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLocalUser: EntityInsertAdapter<LocalUser>

  private val __insertAdapterOfUserPreferences: EntityInsertAdapter<UserPreferences>

  private val __deleteAdapterOfLocalUser: EntityDeleteOrUpdateAdapter<LocalUser>
  init {
    this.__db = __db
    this.__insertAdapterOfLocalUser = object : EntityInsertAdapter<LocalUser>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `Users` (`username`,`password`,`email`,`name`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LocalUser) {
        statement.bindText(1, entity.username)
        statement.bindText(2, entity.password)
        statement.bindText(3, entity.email)
        statement.bindText(4, entity.name)
      }
    }
    this.__insertAdapterOfUserPreferences = object : EntityInsertAdapter<UserPreferences>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `User_Preferences` (`username`,`isLoggedIn`,`appLanguage`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserPreferences) {
        statement.bindText(1, entity.username)
        val _tmp: Int = if (entity.isLoggedIn) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindText(3, entity.appLanguage)
      }
    }
    this.__deleteAdapterOfLocalUser = object : EntityDeleteOrUpdateAdapter<LocalUser>() {
      protected override fun createQuery(): String = "DELETE FROM `Users` WHERE `username` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LocalUser) {
        statement.bindText(1, entity.username)
      }
    }
  }

  public override suspend fun insertUser(user: LocalUser): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfLocalUser.insert(_connection, user)
  }

  public override suspend fun insertUserPreferences(userPreferences: UserPreferences): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserPreferences.insert(_connection, userPreferences)
  }

  public override suspend fun deleteUser(user: LocalUser): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfLocalUser.handle(_connection, user)
  }

  public override suspend fun getUserByUsername(username: String): LocalUser? {
    val _sql: String = "SELECT * FROM Users WHERE username = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: LocalUser?
        if (_stmt.step()) {
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _result = LocalUser(_tmpUsername,_tmpPassword,_tmpEmail,_tmpName)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLoggedInUser(): LocalUser? {
    val _sql: String =
        "SELECT * FROM Users WHERE username IN (SELECT username FROM User_Preferences WHERE isLoggedIn = 1) LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: LocalUser?
        if (_stmt.step()) {
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _result = LocalUser(_tmpUsername,_tmpPassword,_tmpEmail,_tmpName)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun logOutUser(username: String) {
    val _sql: String = "UPDATE User_Preferences SET isLoggedIn = false WHERE username = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun logInUser(username: String) {
    val _sql: String = "UPDATE User_Preferences SET isLoggedIn = true WHERE username = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
