package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.AdditionalInfoRequestDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.NicknameDTO
import com.umc.upstyle.data.model.UserInfoDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {
    // 사용자 추가 정보 입력 API
    @POST("/users/additional-info")
    fun addAdditionalInfo(
        @Header("Authorization") token: String,  // JWT
        @Body request: AdditionalInfoRequestDTO
    ): Call<ApiResponse<UserInfoDTO>>

    // 닉네임 변경 API
    @PATCH("/users/nickname")
    fun updateNickname(
        @Header("Authorization") token: String,
        @Body request: NicknameDTO
    ): Call<ApiResponse<NicknameDTO>>

    // 사용자 정보 조회 API
    @GET("/users/")
    fun getUserInfo(
        @Header("Authorization") token: String
    ): Call<ApiResponse<AccountInfoDTO>>
}