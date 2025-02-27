package com.umc.upstyle.utils

object FitUtil {
    private val fitMap = mapOf(
        "슬림핏" to 1,
        "레귤러핏" to 2,
        "오버핏" to 3,
    )

    fun getFitIdByName(fitName: String?): Int? {
        return fitMap[fitName]
    }
}
