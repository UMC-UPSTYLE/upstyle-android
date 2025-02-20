package com.umc.upstyle.data.network

import com.umc.upstyle.data.model.BookmarkRequest
import com.umc.upstyle.data.model.BookmarkResponse
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.model.ClothesCategoryResponse
import com.umc.upstyle.data.model.RequestResponse
import com.umc.upstyle.data.model.VotePreviewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // 본인 옷장 조회 API
    @GET("closets/")
    fun getMyCloset(
        @Query("userId") userId: Int?= null
    ): Call<ClosetResponse>

    // 특정 사용자 옷장 조회 API
    @GET("closets/{userId}")
    fun getOtherCloset(
        @Path("userId") userId: Int?= null
    ): Call<ClosetResponse>

    // 옷장 카테고리별 조회 API
    @GET("closets/categories")
    fun getClosetByCategory(
        @Query("userId") userId: Int? = null,
        @Query("kindId") kindId: Int? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("colorId") colorId: List<Int>? = null,
        @Query("fitId") fitId: Int? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<ClosetCategoryResponse>

    // 옷장 카테고리별 조회 API
    @GET("closets/categories")
    fun getClosetByFilter(
        @Query("userId") userId: Int?= null,
        @Query("kindId") categoryId: Int? = null,
        @Query("colorId") colorId: List<Int>
    ): Call<ClosetCategoryResponse>


    @GET("closet/{userId}/{categoryId}")
    fun getClosetByCategoryPath(
        @Path("userId") userId: Int,
        @Path("categoryId") categoryId: Int
    ): Call<ClosetCategoryResponse>

    @GET("votes/")
    fun getVotePreviews(): Call<VotePreviewResponse>


    @GET("bookmarks/")  // 서버의 API 엔드포인트
    fun getBookmarks(
        @Query("kindId") kindId: Int? = null
    ): Call<BookmarkResponse>

    @GET("codi-requests/")  // 실제 서버의 API 엔드포인트로 교체
    fun getCodiRequests(): Call<RequestResponse>

    @POST("/bookmarks/")
    fun toggleBookmark(
        @Body request: BookmarkRequest
    ): Call<BookmarkResponse>

    @GET("/clothes/categories")
    fun getClothesByCategory(
        @Query("kindId") kindId: Int? = null,
        @Query("categoryId") categoryId: Int?,
        @Query("colorIds") colorIds: List<Int>?,
        @Query("fitId") fitId: Int?,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<ClothesCategoryResponse>


    @GET("bookmarks/{clothId}")
    fun getBookmarkStatus(
        @Header("Authorization") token: String,
        @Path("clothId") clothId: Int
    ): Call<BookmarkResponse>



}
