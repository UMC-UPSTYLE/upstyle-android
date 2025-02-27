package com.umc.upstyle.data.model

data class AddCodiReqDTO (
    val userId: Int,
    val title: String,
    val body: String,
    val imageUrl: String
)

data class AddCodiReqRes (
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: AddCodiRes
)

data class AddCodiRes (
    val id: Int,
    val title: String,
    val responseCount: Int
)