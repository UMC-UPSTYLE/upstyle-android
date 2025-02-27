package com.umc.upstyle.utils

object SizeUtil {
    private val sizeMap = mapOf(
        "스몰" to 1,
        "미디엄" to 2,
        "라지" to 3
    )

    fun getSizeIdByName(sizeName: String?): Int? {
        return sizeMap[sizeName]
    }
}
