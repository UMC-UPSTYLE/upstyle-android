package com.umc.upstyle.data.model

data class AdditionalInfoRequestDTO(
    val nickname: String,
    val gender: Gender,
    val height: Double?,
    val weight: Double?
)
