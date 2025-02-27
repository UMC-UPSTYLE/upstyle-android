package com.umc.upstyle.data.model

data class KakaoToken(
    val access_token: String,
    val token_type: String,
    val refresh_token: String,
    val expires_in: Int,
    val refresh_token_expires_in: Int,
    val id_token: String
)