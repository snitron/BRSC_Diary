package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName

data class NameModel(
        @SerializedName("child_ids") val child_ids: ArrayList<String>?,
        @SerializedName("name") val name: String?)