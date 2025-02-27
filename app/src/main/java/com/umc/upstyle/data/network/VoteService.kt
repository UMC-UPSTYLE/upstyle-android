package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.VoteDetailResponse
import com.umc.upstyle.data.model.VoteRequest
import com.umc.upstyle.data.model.VoteResponseRequest
import com.umc.upstyle.data.model.VoteResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VoteService {
    @POST("votes/")
    suspend fun createVote(
        @Body request: VoteRequest
    ): Response<ApiResponse<VoteDetailResponse>>

    @GET("votes/{voteId}")
    suspend fun getVoteDetail(
        @Path("voteId") voteId: Int
    ): Response<ApiResponse<VoteDetailResponse>>

    @POST("/votes/{voteId}/response")
    suspend fun submitVoteResponse(
        @Path("voteId") voteId: Int,
        @Body request: VoteResponseRequest
    ): Response<ApiResponse<VoteResult>>
}
