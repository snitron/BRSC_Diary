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
        val items = LinkedList<TableMarks>()

        items.add(TableMarks(average_mark1, m1))
        items.add(TableMarks(average_mark2, m2))
        items.add(TableMarks(average_mark3, m3))
        items.add(TableMarks(average_mark4, m4))

        return Lesson(lesson, items)
    }
}