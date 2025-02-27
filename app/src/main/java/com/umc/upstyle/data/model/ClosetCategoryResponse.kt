package com.umc.upstyle.data.model

import com.google.gson.annotations.SerializedName

data class ClosetCategoryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: ClosetCategoryResult
)

data class ClosetItem(
    val id: Int,
    @SerializedName("image_url") val imageUrl: String,
    val description: String
)
