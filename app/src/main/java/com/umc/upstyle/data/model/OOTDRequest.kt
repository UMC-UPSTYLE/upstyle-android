package com.umc.upstyle.data.model

data class OOTDRequest(
    val userId: Int,
    val date: String,
    val imageUrls: MutableList<String>,
    val clothRequestDTOList: List<ClothRequestDTO>
)