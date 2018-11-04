package com.nitronapps.brsc_diary.Data

import android.os.Parcel
import android.os.Parcelable

class TableMarks(val average_marks: String,
                 val m: String
                 ): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(average_marks)
        parcel.writeString(m)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TableMarks> {
        override fun createFromParcel(parcel: Parcel): TableMarks {
            return TableMarks(parcel)
        }

        override fun newArray(size: Int): Array<TableMarks?> {
            return arrayOfNulls(size)
        }
    }
}



