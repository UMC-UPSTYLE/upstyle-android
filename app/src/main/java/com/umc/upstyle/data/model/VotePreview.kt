package com.umc.upstyle.data.model

data class VotePreview(
    val id: Int,
    val title: String,
    val totalResponseCount: Int
)

data class VotePreviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: VotePreviewResult
)

data class VotePreviewResult(
    val votePreviewList: List<Post>,
    val listSize: Int,
    val totalPage: Int,
    val totalElements: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)
