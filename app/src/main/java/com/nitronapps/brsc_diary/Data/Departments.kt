package com.nitronapps.brsc_diary.Data

import com.google.gson.annotations.SerializedName
import java.util.*

data class Departments(@SerializedName("name") val name: String,
                       @SerializedName("departmentId") val departmentId: String,
                       @SerializedName("yearStart") val yearStart: String,
                       @SerializedName("yearEnd") val yearEnd: String) {

    fun getRightYear(): String {
        if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 1.0..22.0)
            return this.yearEnd
        else if (GregorianCalendar.getInstance().get(GregorianCalendar.WEEK_OF_YEAR) in 35.0..52.0)
            return this.yearStart
        else
            return this.yearStart
    }
}