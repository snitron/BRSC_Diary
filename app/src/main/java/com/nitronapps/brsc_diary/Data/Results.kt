package com.nitronapps.brsc_diary.Data

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class Results(t: String, i:List<ResultsMarks>): ExpandableGroup<ResultsMarks>(t, i){
    internal var title: String? = null
    internal var items: List<ResultsMarks>? = null

}