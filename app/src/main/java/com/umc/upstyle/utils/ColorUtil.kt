package com.umc.upstyle.util

object ColorUtil {
    private val colorMap = mapOf(
        "블랙" to 1,
        "실버" to 2,
        "화이트" to 3,
        "그레이" to 4,
        "레드" to 5,
        "버건디" to 6,
        "핑크" to 7,
        "오렌지" to 8,
        "아이보리" to 9,
        "오트밀" to 10,
        "옐로우" to 11,
        "그린" to 12,
        "카키" to 13,
        "민트" to 14,
        "스카이블루" to 15,
        "블루" to 16,
        "네이비" to 17,
        "퍼플" to 18,
        "브라운" to 19,
        "카멜" to 20,
        "베이지" to 21,
        "연청" to 22,
        "중청" to 23,
        "흑청" to 24,
        "기타색상" to 25
    )

    fun getColorIdByName(name: String): Int? {
        return colorMap[name]
    }
}
