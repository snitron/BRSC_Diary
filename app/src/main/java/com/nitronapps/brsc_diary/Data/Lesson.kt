package com.nitronapps.brsc_diary.Data

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class Lesson(title: String, items: List<TableMarks>) : ExpandableGroup<TableMarks>(title, items) {
    internal var title: String? = null
    internal var items: List<TableMarks>? = null
}
