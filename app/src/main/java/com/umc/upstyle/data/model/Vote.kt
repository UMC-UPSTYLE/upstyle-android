package com.umc.upstyle.data.model

data class VoteRequest(
    val userId: Int,
    val title: String,
    val body: String,
    val imageUrl: String,
    val optionList: List<VoteItem>
)

data class VoteOptionSimple(
    val clothId: Int,
    val imageUrl: String,
    val name: String
)



data class VoteUser(
    val id: Int,
    val nickname: String
)


