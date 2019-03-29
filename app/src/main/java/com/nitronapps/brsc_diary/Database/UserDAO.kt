package com.nitronapps.brsc_diary.Database

import androidx.room.*
import com.nitronapps.brsc_diary.Data.Departments
import com.nitronapps.brsc_diary.Data.User

@Dao
interface UserDAO{
    @Query("SELECT * FROM userdb")
    fun getAllUsers(): List<UserDB>

    @Query("SELECT * FROM userdb WHERE id LIKE :id LIMIT 1")
    fun getUserById(id: Int): UserDB

    @Query("UPDATE userdb SET prefDepartment = :detpartment WHERE id = :id")
    fun setPrefDepartments(id: Int, detpartment: String)

    @Query("UPDATE userdb SET dids = :departments WHERE id = :id")
    fun setDepartments(id: Int, departments: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: UserDB)

    @Delete
    fun deleteAll(user: UserDB)

    @Query("SELECT COUNT(*) FROM userdb")
    fun getDataCount(): Int
}