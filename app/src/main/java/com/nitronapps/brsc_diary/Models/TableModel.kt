package com.nitronapps.brsc_diary.Models

import com.nitronapps.brsc_diary.Data.Lesson
import com.nitronapps.brsc_diary.Data.TableMarks
import java.util.*

data class TableModel(val lesson: String,
                      val average_mark1: String,
                      val average_mark2: String,
                      val average_mark3: String,
                      val average_mark4: String,
                      val m1: String,
                      val m2: String,
                      val m3: String,
                      val m4: String){


    fun getLesson():Lesson{
        val marks = LinkedList<TableMarks>()

        marks.add(TableMarks(average_mark1.trim(), m1.trim()))
        marks.add(TableMarks(average_mark2.trim(), m2.trim()))
        marks.add(TableMarks(average_mark3.trim(), m3.trim()))
        marks.add(TableMarks(average_mark4.trim(), m4.trim()))

        return Lesson(lesson, marks)
    }
}