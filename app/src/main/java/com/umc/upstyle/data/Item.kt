package com.umc.upstyle.data

data class Item(
    val iconRes: Int,        // 아이콘 리소스 ID
    val subCategory: String, // 하위 카테고리 (예: 무스탕, 니트 등)
    val fit: String? = null, // 핏 (예: 오버핏, 슬림 등), 기본값 null
    val size: String? = null, // 사이즈 (예: 미디엄 등), 기본값 null
    val color: String        // 색상 (예: 블랙, 그레이 등)
)
