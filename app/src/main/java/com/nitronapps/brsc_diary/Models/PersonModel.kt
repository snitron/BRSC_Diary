package com.nitronapps.brsc_diary.Models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.annotations.SerializedName

data class PersonModel(@SerializedName("parentName") val parentName:String,
                       @SerializedName("childNames") val childNames:ArrayList<String>)

