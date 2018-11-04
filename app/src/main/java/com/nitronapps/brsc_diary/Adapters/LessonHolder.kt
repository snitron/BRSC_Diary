package com.nitronapps.brsc_diary.Adapters

import android.view.View
import android.widget.TextView


import com.nitronapps.brsc_diary.R
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

class LessonHolder(itemView: View) : GroupViewHolder(itemView) {
    private val title: TextView

    init {
        title = itemView.findViewById(R.id.textViewLessonTitle)
    }

    fun setTitle(group: ExpandableGroup<*>) {
        title.text = group.title
    }

}
