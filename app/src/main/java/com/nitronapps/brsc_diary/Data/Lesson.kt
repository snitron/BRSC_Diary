package com.nitronapps.brsc_diary.Data

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class Lesson(t: String, i: List<TableMarks>) : ExpandableGroup<TableMarks>(t, i) {
    internal var title: String? = null
    internal var items: List<TableMarks>? = null
}
