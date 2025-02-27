package com.umc.upstyle.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 사용자 데이터 클래스
@Parcelize
data class User(
    val id: Int,
    val nickname: String
) : Parcelable