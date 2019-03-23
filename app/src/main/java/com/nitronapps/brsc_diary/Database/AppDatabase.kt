package com.nitronapps.brsc_diary.Database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(UserDB::class), version = 1)
abstract class AppDatabase(): RoomDatabase(){
    abstract fun userDao(): UserDAO
}