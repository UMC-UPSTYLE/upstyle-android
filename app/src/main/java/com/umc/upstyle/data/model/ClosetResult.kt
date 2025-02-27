package com.umc.upstyle.data.model

data class ClosetResult(
    val userId: Int,
    val userName: String,
    val clothKindList: List<ClothKind>
)