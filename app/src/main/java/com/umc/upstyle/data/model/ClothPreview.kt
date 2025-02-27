package com.umc.upstyle.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClothPreview(
    val id: Int,
    val kindId: Int,
    val kindName: String,
    val categoryId: Int,
    val categoryName: String,
    val fitId: Int,
    val fitName: String,
    val colorId: Int,
    val colorName: String,
    val additionalInfo: String?,
    val ootd: Ootd? = null
) : Parcelable
{
    @Parcelize
    data class Ootd(
        val imageUrl: String,
    ) : Parcelable
}
