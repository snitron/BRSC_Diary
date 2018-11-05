package com.nitronapps.brsc_diary.Adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import com.nitronapps.brsc_diary.Data.TableMarks
import com.nitronapps.brsc_diary.R

import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder

class MarksHolder(itemView: View) : ChildViewHolder(itemView){
    private val position:TextView
    private val marks:TextView
    private val averageMark:TextView

    init {
        position = itemView.findViewById(R.id.textViewPositionMarks)
        marks = itemView.findViewById(R.id.textViewMarks)
        averageMark = itemView.findViewById(R.id.textViewAverageMark)
    }

    fun onBind(tableMarks: TableMarks, number: Int){
        averageMark.setBackgroundColor(Color.WHITE)
        marks.text = tableMarks.m.trim()
        averageMark.text = tableMarks.average_marks.trim()
        averageMark.setTextColor(Color.BLACK)

        Log.w("am", number.toString() + tableMarks.average_marks.trim() + ".")
        if(!tableMarks.average_marks.trim().equals("")) {
            if (tableMarks.average_marks.replace(',', '.').toDouble() <= 2.5)
                averageMark.setBackgroundColor(Color.parseColor("#e74c3c"))
            if (tableMarks.average_marks.replace(',', '.').toDouble() in 2.5..3.5)
                averageMark.setBackgroundColor(Color.parseColor("#f1c40f"))
            if (tableMarks.average_marks.replace(',', '.').toDouble() in 3.5..5.0)
                averageMark.setBackgroundColor(Color.parseColor("#27ae60"))
        }

        when (number){
            0 -> position.text = "I"
            1 -> position.text = "II"
            2 -> position.text = "III"
            3 -> position.text = "IV"
            else -> {}
        }
    }

}