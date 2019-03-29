package com.nitronapps.brsc_diary.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserDB(
        @PrimaryKey  var id: Int,
        @ColumnInfo(name = "login") var login: String,
        @ColumnInfo(name = "password") var password: String,
        @ColumnInfo(name = "uid") var uid: String, // JSON в виде UserInfoModel
        @ColumnInfo(name = "dids") var dids: String, // (departmensIds) JSON в виде Array<Departments>
        @ColumnInfo(name = "name") var name: String, // JSON of NameModel
        @ColumnInfo(name = "prefDepartment") var prefDepartment: String // JSON array of pref Department IDs
)