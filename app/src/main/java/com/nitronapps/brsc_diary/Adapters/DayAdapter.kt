package com.nitronapps.brsc_diary.Adapters

import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nitronapps.brsc_diary.Models.DayModel
import com.nitronapps.brsc_diary.MainActivity
import com.nitronapps.brsc_diary.R
import com.nitronapps.brsc_diary.Adapters.LessonsAdapter


class DayAdapter(val days: Array<DayModel>, val context: Context): RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DayViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_day, p0, false)
        return DayViewHolder(view)
    }

    override fun getItemCount(): Int {
        return days.size
    }

    override fun onBindViewHolder(p0: DayViewHolder, p1: Int) {
        p0.day.text = days[p1].dayName
        p0.lessons.layoutManager = LinearLayoutManager(context)
        p0.lessons.adapter = LessonsAdapter(days[p1], context)
        p0.lessons.addItemDecoration(MainActivity.SpacesItemDecoration(10))
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardView = itemView.findViewById<CardView>(R.id.cardViewDay)
        val day = itemView.findViewById<TextView>(R.id.textViewDay)
        val lessons = itemView.findViewById<RecyclerView>(R.id.recyclerViewDay)

    }
}