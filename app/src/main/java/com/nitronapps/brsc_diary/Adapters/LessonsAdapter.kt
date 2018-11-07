package com.nitronapps.brsc_diary.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import com.nitronapps.brsc_diary.Data.*
import com.nitronapps.brsc_diary.MainActivity
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.R

 class LessonsAdapter(val dayModel: DayModel, val context: Context) : RecyclerView.Adapter<LessonsAdapter.LessonsViewHolder>() {

    val SPACES = 0
    val FILES = 1

    override fun getItemCount(): Int {
        return dayModel.count
    }

    override fun onBindViewHolder(p0: LessonsViewHolder, p1: Int) {
        if (!dayModel.isWeekend) {
            p0.mark.setBackgroundColor(Color.WHITE)
            p0.mark.setTextColor(Color.BLACK)

            p0.position.text = Html.fromHtml((p1 + 1).toString())
            p0.lesson.text = Html.fromHtml(dayModel.lessons[p1])
            p0.homework.text = deleteSpaces(Html.fromHtml(dayModel.homeworks[p1]).toString(), FILES)

            if(dayModel.hrefHw != null && dayModel.hrefHwNames != null)
                if(dayModel.hrefHw[p1] != null && dayModel.hrefHwNames[p1] != null)
                    p0.homework.text = p0.homework.text as String + "\n\n Зажмите и удерживайте для просмотра приложенных файлов."



            val mark = deleteSpaces(Html.fromHtml(dayModel.marks[p1]).toString(), SPACES).trim()

            if(mark.isNotEmpty())
                when(mark[mark.length - 1]){
                    '2' -> {
                        p0.mark.setBackgroundColor(Color.parseColor(RED_BCK))
                        p0.mark.setTextColor(Color.parseColor(RED_TXT))
                    }

                    '3' -> {
                        p0.mark.setBackgroundColor(Color.parseColor(YELLOW_BCK))
                        p0.mark.setTextColor(Color.parseColor(YELLOW_TXT))
                    }

                    '4', '5' -> {
                        p0.mark.setBackgroundColor(Color.parseColor(GREEN_BCK))
                        p0.mark.setTextColor(Color.parseColor(GREEN_TXT))
                    }

                    'H', 'Н', 'н' -> {
                        p0.mark.setBackgroundColor(Color.parseColor(GREY_BCK))
                        p0.mark.setTextColor(Color.BLACK)
                    }

                    else -> {}
                }

            p0.mark.text = mark

            if(dayModel.teacherComment[p1] != null)
                p0.mark.text  = p0.mark.text as String + " (i)"

            p0.homework.setOnLongClickListener({
                if(dayModel.hrefHw != null && dayModel.hrefHwNames != null)
                    if(dayModel.hrefHw[p1] != null && dayModel.hrefHwNames[p1] != null) {
                        AlertDialog.Builder(it.context)
                                .setTitle("Файлы:")
                                .setItems(dayModel.hrefHwNames[p1], object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dayModel.hrefHw[p1]?.get(which))))
                                    }
                                }).create()
                                .show()


                    }
                return@setOnLongClickListener true
                    })

            p0.mark.setOnLongClickListener({
                if(dayModel.teacherComment[p1] != null) {
                        AlertDialog.Builder(it.context)
                                .setTitle("Комментарий учителя:")
                                .setMessage(dayModel.teacherComment[p1]?.trim())
                                .create()
                                .show()
                                }
                return@setOnLongClickListener true
            })

            p0.mark.setOnClickListener({
                if(dayModel.teacherComment[p1] != null)
                    if(!dayModel.teacherComment[p1]?.trim().equals(""))
                        Toast.makeText(it.context, "Для просмотра комментария зажмите и удерживайте значок (i)", Toast.LENGTH_LONG).show()
            })


        } else {
            if (p1 == 0) {
                p0.homework.text = "Выходной"
                p0.position.text = ""
                p0.lesson.text = ""
                p0.mark.text = ""
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LessonsViewHolder {
        return LessonsViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_lesson, p0, false))
    }

    fun deleteSpaces(string: String, option: Int): String{
        var length = 0
        if(option == SPACES)
            for (i in 0 until string.length) {
                length++
                if(i + 2 < string.length)
                    if(string[i] == ' ' && string[i + 2] != ' ')
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


    class LessonsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.cardViewLesson)
        val position = itemView.findViewById<TextView>(R.id.textViewNumber)
        val lesson = itemView.findViewById<TextView>(R.id.textViewLesson)
        val homework = itemView.findViewById<TextView>(R.id.textViewHomework)
        val mark = itemView.findViewById<TextView>(R.id.textViewMark)
    }
}