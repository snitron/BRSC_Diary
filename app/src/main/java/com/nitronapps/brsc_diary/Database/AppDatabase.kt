package com.nitronapps.brsc_diary.Database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(UserDB::class), version = 4)
abstract class AppDatabase(): RoomDatabase(){
    abstract fun userDao(): UserDAO
}