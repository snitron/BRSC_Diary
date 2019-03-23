package com.nitronapps.brsc_diary.Models

import com.google.gson.annotations.SerializedName

data class UserModel(@SerializedName("isParent") val isParent: Boolean,
                    @SerializedName("parentName") val parentName: String?,
                    @SerializedName("child_ids") val childIds: Array<UserInfoModel>)


