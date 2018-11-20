package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName

data class UserModel(@SerializedName("child_ids") val child_ids: Array<Int>?,
                     @SerializedName("id") val id: Int?,
                     @SerializedName("parent_id") val parent_id: String?)

