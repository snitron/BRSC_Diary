package com.nitronapps.brsc_diary.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nitronapps.brsc_diary.Data.Results
import com.nitronapps.brsc_diary.Data.ResultsMarks
import com.nitronapps.brsc_diary.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class ResultsAdapter(groups: List<ExpandableGroup<*>>) : ExpandableRecyclerViewAdapter<LessonHolder, ResultsMarksHolder>(groups) {
    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): LessonHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_lesson_table, parent, false)
        return LessonHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ResultsMarksHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_results, parent, false)
        return ResultsMarksHolder(view)
    }

    override fun onBindChildViewHolder(holder: ResultsMarksHolder?, flatPosition: Int, group: ExpandableGroup<*>?, childIndex: Int) {
        val item = group?.items?.get(childIndex) as ResultsMarks
        holder?.onBind(item)
    }

    override fun onBindGroupViewHolder(holder: LessonHolder?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder?.setTitle(group!!)
    }
}