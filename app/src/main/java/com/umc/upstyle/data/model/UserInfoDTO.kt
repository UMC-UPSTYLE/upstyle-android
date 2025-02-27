package com.umc.upstyle.data.model

data class UserInfoDTO(
    val id: Int,
    val nickname: String,
    val email: String,
    val gender: Gender,
    val height: Double?,
    val weight: Double?
)
