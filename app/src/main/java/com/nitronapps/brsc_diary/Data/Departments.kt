package com.nitronapps.brsc_diary.Data

import com.google.gson.annotations.SerializedName

data class Departments(@SerializedName("name") val name: String,
                       @SerializedName("departmentId") val depatmentId: String)