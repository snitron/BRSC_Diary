package com.nitronapps.brsc_diary.Data

import com.google.gson.annotations.SerializedName

data class Info(@SerializedName("name") val name: String,
                @SerializedName("value") val value: String)