package com.umc.upstyle.data.model

// 공통 API 응답 데이터 클래스 (ApiResponseOotd, ApiResponseCalendar 에서 사용)
data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T
)
