package com.nitronapps.brsc_diary.Models

class DayModel internal constructor(val count: Int,
                                    val lessons: Array<String>,
                                    val homeworks: Array<String>,
                                    val marks: Array<String>,
                                    val isWeekend: Boolean,
                                    val dayName: String,
                                    val teacherComment: String,
                                    val hrefHw: Array<Array<String>>)
