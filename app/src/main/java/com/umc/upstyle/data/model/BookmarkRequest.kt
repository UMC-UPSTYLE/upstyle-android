package com.umc.upstyle.data.model

import com.google.gson.annotations.SerializedName

data class BookmarkRequest(
    val userId: Int,
    @SerializedName("clothId")
    val clothId: Int
)
