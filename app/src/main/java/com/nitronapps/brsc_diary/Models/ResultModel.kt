package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName
import com.nitronapps.brsc_diary.Data.Results
import com.nitronapps.brsc_diary.Data.ResultsMarks
import java.util.*

data class ResultModel(@SerializedName("lesson") val lesson: String,
                       @SerializedName("m1") val m1: String,
                       @SerializedName("m2") val m2: String,
                       @SerializedName("m3") val m3: String,
                       @SerializedName("m4") val m4: String,
                       @SerializedName("y") val y: String,
                       @SerializedName("res") val res: String,
                       @SerializedName("test") val test: String,
                       @SerializedName("isHalfYear") val isHalfYear: Boolean){

    fun getResults():Results{
        val results = LinkedList<ResultsMarks>();

        results.add(ResultsMarks(m1, m2, m3, m4, y, res, test, isHalfYear))

        return(Results(lesson, results))
    }
}