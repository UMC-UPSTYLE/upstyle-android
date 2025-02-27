package com.umc.upstyle.data.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class ClothRequestDTO(
    val clothId: Int = 0,
    val clothKindId: Int = 0,
    val clothCategoryId: Int = 0,
    val fitCategoryId: Int = 0,
    val colorCategoryId: Int = 0,
    val additionalInfo: String = ""
) : Parcelable


@Parcelize
data class ClothRequestDesDTO(
    val clothId: Int = 0,
    val clothKindId: Int = 0,
    val clothCategoryId: Int = 0,
    val fitCategoryId: Int = 0,
    val colorCategoryId: Int = 0,
    val additionalInfo: String = "",
    val description: String = ""
) : Parcelable