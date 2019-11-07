package com.nitronapps.brsc_diary.Data

import com.google.gson.annotations.SerializedName

data class Balance(@SerializedName("name") val name: String,
                   @SerializedName("bill") val bill: String,
                   @SerializedName("balance") val balance: String,
                   @SerializedName("pay") val pay: String?,
                   @SerializedName("child_name") val childName: String?)