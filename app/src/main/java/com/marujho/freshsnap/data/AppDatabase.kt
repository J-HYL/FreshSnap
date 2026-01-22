package com.marujho.freshsnap.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.marujho.freshsnap.data.dao.UserDao
import com.marujho.freshsnap.data.model.User

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}