package com.umc.upstyle.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ootd(
    val id: Int,
    val user: User,
    val date: String,
    val imageUrls: List<String>,
    val clothList: List<ClothPreview>
) : Parcelable
