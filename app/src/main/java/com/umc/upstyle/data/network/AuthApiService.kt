package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.KakaoJwtDTO
import com.umc.upstyle.data.model.KakaoToken
import com.umc.upstyle.data.model.OOTDCalendar
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
//    @GET("/auth/login/kakao")
//    fun loginWithKakao(
//        @Header("Authorization") accessToken: String // 카카오 액세스 토큰을 헤더에 포함
//    ): Call<ApiResponse<KakaoJwtDTO>>
    @POST("/auth/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<ApiResponse<String>>

    @GET("/auth/kakao/loginJWT")
    fun loginWithKakao(
        @Query("access_token") accessToken: String // 카카오 액세스 토큰을 쿼리 파라미터로 포함
    ): Call<String>

    @GET("/auth/kakao/callback")
    fun getKakaoToken(
        @Query("code") code: String // 카카오 인가 코드를 쿼리 파라미터로 포함
    ): Call<KakaoToken>

}

