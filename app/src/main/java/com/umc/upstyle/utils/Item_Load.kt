package com.umc.upstyle.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item_load(
    val description: String,
    val imageUrl: String,
    var isSelected: Boolean = false,

    val id: Int,
    val kindId: Int,
    val categoryId: Int,
    val fitId: Int,
    val colorId: Int,
    val addInfo: String
) : Parcelable {
}