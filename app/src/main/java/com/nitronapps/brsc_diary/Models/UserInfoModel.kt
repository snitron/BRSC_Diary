package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName

data class UserInfoModel(@SerializedName("rooId") val rooId: String,
                         @SerializedName("instituteId") val instituteId: String,
                         @SerializedName("departmentId") val departmentId: String,
                         @SerializedName("userId") val userId: String,
                         @SerializedName("userName") val userName: String)