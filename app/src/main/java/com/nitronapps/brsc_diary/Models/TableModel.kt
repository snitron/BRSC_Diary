package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName
import com.nitronapps.brsc_diary.Data.Lesson
import com.nitronapps.brsc_diary.Data.TableMarks
import java.util.*

data class TableModel(@SerializedName("lesson") val lesson: String,
                      @SerializedName("average_mark1") val average_mark1: String,
                      @SerializedName("average_mark2") val average_mark2: String,
                      @SerializedName("average_mark3") val average_mark3: String,
                      @SerializedName("average_mark4") val average_mark4: String,
                      @SerializedName("m1") val m1: String,
                      @SerializedName("m2") val m2: String,
                      @SerializedName("m3") val m3: String,
                      @SerializedName("m4") val m4: String){


    fun getLesson():Lesson{
        val marks = LinkedList<TableMarks>()

        marks.add(TableMarks(average_mark1.trim(), m1.trim()))
        marks.add(TableMarks(average_mark2.trim(), m2.trim()))
        marks.add(TableMarks(average_mark3.trim(), m3.trim()))
        marks.add(TableMarks(average_mark4.trim(), m4.trim()))

        return Lesson(lesson, marks)
    }
}