package com.nitronapps.brsc_diary.Adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nitronapps.brsc_diary.Models.ResultModel
import com.nitronapps.brsc_diary.R

class ResultsAdapter(val data: Array<ResultModel>) : RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ResultsViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_results, p0, false)
        return ResultsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(p0: ResultsViewHolder, p1: Int) {
        p0.lesson.text = data[p1].lesson
        p0.m1.text = data[p1].m1.replace(" ", "")
        p0.m2.text = data[p1].m2.replace(" ", "")

        if(!data[p1].isHalfYear)
            p0.m3.text = data[p1].m3.replace(" ", "")
            p0.m4.text = data[p1].m4.replace(" ", "")


        p0.y.text = data[p1].y.replace(" ", "")
        p0.res.text = data[p1].res.replace(" ", "")
        p0.test.text = data[p1].test.replace(" ", "")
    }

    class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lesson: TextView
        val m1: TextView
        val m2: TextView
        val m3: TextView
        val m4: TextView
        val y: TextView
        val res: TextView
        val test: TextView

        init {
            lesson = itemView.findViewById(R.id.textViewLessonResult)
            m1 = itemView.findViewById(R.id.textViewFirst)
            m2 = itemView.findViewById(R.id.textViewSecond)
            m3 = itemView.findViewById(R.id.textViewThird)
            m4 = itemView.findViewById(R.id.textViewFour)
            y = itemView.findViewById(R.id.textViewY)
            res = itemView.findViewById(R.id.textViewRes)
            test = itemView.findViewById(R.id.textViewTest)
        }
    }
}