package com.umc.upstyle.data.network

// OotdApiService.kt

import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.OOTDCalendar
import com.umc.upstyle.data.model.Ootd
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OotdApiService {

    @GET("/ootds/{ootdId}")
    fun getOOTDById(@Path("ootdId") ootdId: Int): Call<ApiResponse<Ootd>>

    @GET("/ootds/calendar")
    fun getOOTDCalendar(
        @Query("userId") userId: Int,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Call<ApiResponse<OOTDCalendar>>

}

