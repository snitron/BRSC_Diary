package com.nitronapps.brsc_diary.Models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

data class PersonModel(val name:String, val img:String){
    fun decodeImage():Bitmap{
        val base64image = img.split(",")[1]

        val byteArray = Base64.decode(base64image, Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}