package com.umc.upstyle.data.model

data class VoteDetailResponse(
    val id: Int,
    val user: User,
    val title: String,
    val body: String,
    val imageUrl: String,
    val optionList: List<VoteOption>
)

data class VoteOption(
    val id: Int,
    val clothId: Int,
    val imageUrl: String,
    val name: String,
    val responseCount: Int
)

data class VoteResponseRequest(
    val userId: Int,
    val optionId: Int
)

data class VoteResult(
    val voteResultList: List<VoteOption>
)