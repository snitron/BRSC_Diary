package com.nitronapps.brsc_diary.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.nitronapps.brsc_diary.Data.Balance
import com.nitronapps.brsc_diary.R
import kotlinx.android.synthetic.main.item_balance.view.*
import org.w3c.dom.Text

class BalanceAdapter(val list: ArrayList<Balance>): RecyclerView.Adapter<BalanceAdapter.BalanceHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_balance, parent, false)
        return BalanceHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BalanceHolder, position: Int) {
        holder.textViewName.text = list[position].name
        holder.textViewNumber.text = "Cчёт " + list[position].bill
        holder.textViewValue.text = list[position].balance
    }

    class BalanceHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cardView  = itemView.findViewById<CardView>(R.id.cardViewBalance)
        val textViewName = itemView.findViewById<TextView>(R.id.textViewBalanceName)
        val textViewNumber = itemView.findViewById<TextView>(R.id.textViewBalanceNumber)
        val textViewValue = itemView.findViewById<TextView>(R.id.textViewBalanceValue)
    }
}