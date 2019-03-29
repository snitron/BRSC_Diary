package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName

class DayModel internal constructor(@SerializedName("count") val count: Int,
                                    @SerializedName("lessons") val lessons: Array<String>,
                                    @SerializedName("homeworks") val homeworks: Array<String>,
                                    @SerializedName("marks") val marks: Array<String>,
                                    @SerializedName("isWeekend") val isWeekend: Boolean,
                                    @SerializedName("dayName") val dayName: String,
                                    @SerializedName("teacherComment") val teacherComment: Array<String?>,
                                    @SerializedName("hrefHw") val hrefHw: Array<Array<String>?>?,
                                    @SerializedName("hrefHwNames") val hrefHwNames: Array<Array<String>?>?){
    fun printModel(): String{
        var result = ""

        result += dayName + "\n"
        result += count.toString() + "\n"

        return result
    }
}
