package com.umc.upstyle.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 옷 데이터 클래스
@Parcelize
data class Cloth(
    val id: Int,
    val kindId: Int,
    val kindName: String,
    val categoryId: Int,
    val categoryName: String,
    val fitId: Int,
    val fitName: String,
    val colorId: Int,
    val colorName: String
) : Parcelable