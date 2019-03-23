package com.nitronapps.brsc_diary.Database

import androidx.room.*
import com.nitronapps.brsc_diary.Data.User

@Dao
interface UserDAO{
    @Query("SELECT * FROM userdb")
    fun getAllUsers(): List<UserDB>

    @Query("SELECT * FROM userdb WHERE id LIKE :id LIMIT 1")
    fun getUserById(id: Int): UserDB

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: UserDB)

    @Delete
    fun deleteAll(user: UserDB)
}