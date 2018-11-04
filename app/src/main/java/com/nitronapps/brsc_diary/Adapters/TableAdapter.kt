package com.nitronapps.brsc_diary.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nitronapps.brsc_diary.Data.TableMarks
import com.nitronapps.brsc_diary.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class TableAdapter(groups:List<ExpandableGroup<*>>): ExpandableRecyclerViewAdapter<LessonHolder, MarksHolder>(groups) {
    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): LessonHolder {
       val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_lesson_table, parent, false)
       return LessonHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): MarksHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_table_marks, parent, false)
        return MarksHolder(view)
    }

    override fun onBindChildViewHolder(holder: MarksHolder?, flatPosition: Int, group: ExpandableGroup<*>?, childIndex: Int) {
        val item = group?.items?.get(childIndex) as TableMarks
        holder?.onBind(item, childIndex)
    }

    override fun onBindGroupViewHolder(holder: LessonHolder?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder?.setTitle(group!!)
    }
}