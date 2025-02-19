package com.umc.upstyle.data.model

import com.google.gson.annotations.SerializedName

data class BookmarkRequest(
    @SerializedName("clothId")
    val clothId: Int
)
