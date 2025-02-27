package com.umc.upstyle.data.model

// OOTD 캘린더 미리보기 데이터 클래스
data class OOTDPreview(
    val id: Int,
    val date: String,
    val imageUrl: String,
    val kindId : Int
)