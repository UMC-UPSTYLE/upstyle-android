package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.AddCodiReqDTO
import com.umc.upstyle.data.model.AddCodiRes
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.CodiResponseRequest
import com.umc.upstyle.data.model.RequestDetailResponse
import com.umc.upstyle.data.model.ResponseDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RequestService {
    @POST("codi-requests/")
    suspend fun createCodiReq(
        @Body request: AddCodiReqDTO
    ): Response<ApiResponse<AddCodiRes>>

    @GET("codi-requests/{requestId}")
    suspend fun getRequestDetail(
        @Path("requestId") requestId: Int
    ): Response<ApiResponse<RequestDetailResponse>>

    @GET("codi-requests/response/{responseId}")
    suspend fun getResponseDetail(
        @Path("responseId") responseId: Int
    ): Response<ApiResponse<ResponseDetailResponse>>

    @POST("codi-requests/{requestId}/response")
    suspend fun postCodiResponse(
        @Path("requestId") requestId: Int,
        @Body request: CodiResponseRequest
    ): Response<Unit>

}