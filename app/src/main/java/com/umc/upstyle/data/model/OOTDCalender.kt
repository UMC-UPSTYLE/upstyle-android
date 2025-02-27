package com.umc.upstyle.data.model

// OOTD 캘린더 데이터 클래스
data class OOTDCalendar(
    val userId: Int,
    val ootdPreviewList: List<OOTDPreview>
)