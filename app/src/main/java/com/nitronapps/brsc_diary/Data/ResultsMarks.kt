package com.nitronapps.brsc_diary.Data

import android.os.Parcel
import android.os.Parcelable

class ResultsMarks(val m1: String,
                   val m2: String,
                   val m3: String,
                   val m4: String,
                   val y: String,
                   val res: String,
                   val test: String,
                   val isHalfYear: Boolean):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(m1)
        parcel.writeString(m2)
        parcel.writeString(m3)
        parcel.writeString(m4)
        parcel.writeString(y)
        parcel.writeString(res)
        parcel.writeString(test)
        parcel.writeByte(if (isHalfYear) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResultsMarks> {
        override fun createFromParcel(parcel: Parcel): ResultsMarks {
            return ResultsMarks(parcel)
        }

        override fun newArray(size: Int): Array<ResultsMarks?> {
            return arrayOfNulls(size)
        }
    }
}