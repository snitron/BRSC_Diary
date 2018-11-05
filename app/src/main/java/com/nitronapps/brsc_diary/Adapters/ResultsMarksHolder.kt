package com.nitronapps.brsc_diary.Adapters

import android.view.View
import android.widget.TextView
import com.nitronapps.brsc_diary.Data.ResultsMarks
import com.nitronapps.brsc_diary.R
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
class ResultsMarksHolder(itemView: View): ChildViewHolder(itemView){
    private val m1: TextView
    private val m2: TextView
    private val m3: TextView
    private val m4: TextView
    private val y: TextView
    private val res: TextView
    private val m3Name: TextView
    private val m4Name: TextView


    init {
        m1 = itemView.findViewById(R.id.textViewFirst)
        m2 = itemView.findViewById(R.id.textViewSecond)
        m3 = itemView.findViewById(R.id.textViewThird)
        m4 = itemView.findViewById(R.id.textViewFour)
        y = itemView.findViewById(R.id.textViewY)
        res = itemView.findViewById(R.id.textViewRes)
        m3Name = itemView.findViewById(R.id.textViewThirdName)
        m4Name = itemView.findViewById(R.id.textViewFourName)
    }

    fun onBind(item: ResultsMarks){
        m3Name.visibility = View.VISIBLE
        m4Name.visibility = View.VISIBLE

        m1.text = item.m1
        m2.text = item.m2

        if(item.isHalfYear){
            m3Name.visibility = View.INVISIBLE
            m4Name.visibility = View.INVISIBLE
        } else {
            m3.text = item.m3
            m4.text = item.m4
        }

        y.text = item.y
        res.text = item.res
    }
}