package com.example.linkup.activities.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocalUser::class, UserPreferences::class, Blocks::class], version = 6, exportSchema = false)
abstract class LocalDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var INSTANCE : LocalDatabase? = null

        fun getDB(context : Context) : LocalDatabase{
            val instance = INSTANCE
            if(instance!=null){     //DB already exists
                return instance
            }
            synchronized(this) {
                val newInstance = Room.databaseBuilder(
                                context.applicationContext,
                                LocalDatabase::class.java,
                                "app_user_db"
                            ).fallbackToDestructiveMigration(true).build()
                INSTANCE = newInstance
                return newInstance
            }
        }
    }
}