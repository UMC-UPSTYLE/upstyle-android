package com.umc.upstyle.data.repository

import com.umc.upstyle.data.model.Request
import com.umc.upstyle.data.model.RequestResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RequestRepository {
    fun fetchRequests(callback: (List<Request>) -> Unit) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getCodiRequests().enqueue(object : Callback<RequestResponse> {
            override fun onResponse(call: Call<RequestResponse>, response: Response<RequestResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val serverResponseList = response.body()?.result?.codiReqPreviewList ?: emptyList()

                    val requestList = serverResponseList.map {
                        Request(
                            id = it.id,
                            title = it.title,
                            commentCount = it.responseCount
                        )
                    }

                    callback(requestList) // 콜백을 통해 데이터를 전달
                } else {
                    callback(emptyList()) // 실패 시 빈 리스트 반환
                }
            }

            override fun onFailure(call: Call<RequestResponse>, t: Throwable) {
                callback(emptyList()) // 네트워크 오류 시 빈 리스트 반환
            }
        })
    }
}
