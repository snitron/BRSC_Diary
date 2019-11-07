package com.nitronapps.brsc_diary.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nitronapps.brsc_diary.Data.Info
import com.nitronapps.brsc_diary.R
import kotlinx.android.synthetic.main.item_info.view.*

class InfoAdapter(val list: ArrayList<Info>): RecyclerView.Adapter<InfoAdapter.InfoHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_info, parent, false)

        return InfoHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.textViewName.text = list[position].name
        holder.textViewValue.text = list[position].value
    }


    class InfoHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cardView = itemView.findViewById<CardView>(R.id.cardViewInfo)
        val textViewName = itemView.findViewById<TextView>(R.id.textViewInfoName)
        val textViewValue = itemView.findViewById<TextView>(R.id.textViewInfoValue)
    }
}