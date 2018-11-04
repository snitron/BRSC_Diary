package com.nitronapps.brsc_diary.Adapters

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.R

class LessonsAdapter(val dayModel: DayModel) : RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    val SPACES = 0
    val FILES = 1

    override fun getItemCount(): Int {
        return dayModel.count
    }

    override fun onBindViewHolder(p0: LessonsViewHolder, p1: Int) {
        if (!dayModel.isWeekend) {
            p0.position.text = Html.fromHtml((p1 + 1).toString())
            p0.lesson.text = Html.fromHtml(dayModel.lessons[p1])
            p0.homework.text = deleteSpaces(Html.fromHtml(dayModel.homeworks[p1]).toString(), FILES)
            p0.mark.text = Html.fromHtml(prettyMarks(deleteSpaces(Html.fromHtml(dayModel.marks[p1]).toString(), SPACES)))
        } else
            if (p1 == 0) {
                p0.homework.text = "Выходной"
                p0.position.text = ""
                p0.lesson.text = ""
                p0.mark.text = ""
            }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LessonsViewHolder {
        return LessonsViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_lesson, p0, false))
    }

    fun deleteSpaces(string: String, option: Int): String{
        var length = 0
        if(option == SPACES)
            for (i in string.iterator()) {
                length++
                if(i == ' ')
                    break
            }

        if(option == FILES){
            if(!string.contains("Файлы, прикрепленные учителем"))
                length = string.length
            else{
                length = string.indexOf("Файлы, прикрепленные учителем") - 1
            }
        }
        return string.substring(0, length)
    }

    fun prettyMarks(string: String): String{
        var result = ""
        for(i in string.iterator())
            when(i){
                '2' -> result += "<p style=\"color:#e74c3c; align: center\">2</p>"
                '3' -> result += "<p style=\"color:#ffd32a; align: center\">3</p>"
                '4' -> result += "<p style=\"color:#2ecc71; align: center\">4</p>"
                '5' -> result += "<p style=\"color:#2ecc71; align: center\">5</p>"
                else -> result += i
            }
        return result
    }


    class LessonsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.cardViewLesson)
        val position = itemView.findViewById<TextView>(R.id.textViewNumber)
        val lesson = itemView.findViewById<TextView>(R.id.textViewLesson)
        val homework = itemView.findViewById<TextView>(R.id.textViewHomework)
        val mark = itemView.findViewById<TextView>(R.id.textViewMark)
    }
}

