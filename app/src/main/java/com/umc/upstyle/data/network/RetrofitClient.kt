package com.umc.upstyle.data.network

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.umc.upstyle.GlobalApplication
import com.umc.upstyle.network.AuthInterceptor
import com.umc.upstyle.utils.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://ec2-3-37-185-8.ap-northeast-2.compute.amazonaws.com:8080/"

    private val gson = GsonBuilder().setLenient().create()

    private fun getJwtToken(): String {
        val sharedPreferences = GlobalApplication.instance.getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", "") ?: ""
        Log.d("RetrofitClient", "JWT Token: $token") // 로그 추가
        return token
    }



    // ✅ OkHttpClient에 Interceptor 추가 (Context 필요 없이 JWT 처리)
    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(getJwtToken())) // JWT 토큰을 인터셉터에 바로 넘김
            .build()
    }

    // ✅ `Context` 없이 `Retrofit` 인스턴스 생성
    fun <T> createService(service: Class<T>): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient()) // 인터셉터가 적용된 클라이언트
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(service)
    }
}
