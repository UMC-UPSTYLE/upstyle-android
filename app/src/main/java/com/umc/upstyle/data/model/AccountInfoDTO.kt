package com.umc.upstyle.data.model

data class AccountInfoDTO(
    val email: String,
    val nickname: String,
    val height: Double?,
    val weight: Double?,
    val gender: Gender
)
