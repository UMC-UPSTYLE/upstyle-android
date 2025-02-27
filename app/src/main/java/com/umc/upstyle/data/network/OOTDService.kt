package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.OOTDRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OOTDService {
    @POST("ootds/")
    suspend fun uploadOOTD(@Body updatedRequest: OOTDRequest): ApiResponse<Void>
}

