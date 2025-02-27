// PostRepository.kt
package com.umc.upstyle.data.repository

import android.util.Log  // Log 사용을 위해 추가
import com.umc.upstyle.data.model.Post
import com.umc.upstyle.data.model.VotePreviewResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PostRepository {
    private const val TAG = "PostRepository"

    fun fetchPosts(callback: (List<Post>) -> Unit) {
        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getVotePreviews().enqueue(object : Callback<VotePreviewResponse> {
            override fun onResponse(
                call: Call<VotePreviewResponse>,
                response: Response<VotePreviewResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val postList = response.body()?.result?.votePreviewList ?: emptyList()
                    Log.d(TAG, "데이터 불러오기 성공: $postList")  // 성공 로그 추가
                    callback(postList)
                } else {
                    Log.e(TAG, "서버 응답 실패: ${response.errorBody()?.string()}")  // 응답 실패 로그
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<VotePreviewResponse>, t: Throwable) {
                Log.e(TAG, "네트워크 오류 발생: ${t.message}")  // 네트워크 오류 로그
                callback(emptyList())
            }
        })
    }
}
