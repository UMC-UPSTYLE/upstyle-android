package com.umc.upstyle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.ClothIdResponse
import com.umc.upstyle.data.model.ClothRequestDTO
import com.umc.upstyle.data.model.ClothRequestDesDTO
import com.umc.upstyle.data.model.CodiResponseRequest
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.OOTDService
import com.umc.upstyle.data.network.OotdApiService
import com.umc.upstyle.data.network.RequestService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.databinding.FragmentCodiBottomSheetDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class CodiBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCodiBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var clothList: MutableList<ClothRequestDesDTO>
    private lateinit var clothIDList: MutableList<ClothIdResponse>
    private var requestId by Delegates.notNull<Int>()
    private val apiService = RetrofitClient.createService(OotdApiService::class.java)
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCodiBottomSheetDialogBinding.inflate(inflater, container, false)

        // Bundle에서 clothList와 clothIDList를 받음
        arguments?.let {
            clothList = it.getParcelableArrayList<ClothRequestDesDTO>("CLOTH_LIST") ?: mutableListOf()
            clothIDList = it.getParcelableArrayList<ClothIdResponse>("CLOTH_ID_LIST") ?: mutableListOf()
            requestId = it.getInt("REQUEST_ID", -1) // 기본값 -1 설정 (예외 처리용)
        }

        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)

        val apiService = RetrofitClient.createService(ApiService::class.java)

        userApiService.getUserInfo("Bearer $jwtToken").enqueue(object :
            Callback<ApiResponse<AccountInfoDTO>> {
            override fun onResponse(
                call: Call<ApiResponse<AccountInfoDTO>>,
                response: Response<ApiResponse<AccountInfoDTO>>
            ) {
                if (response.isSuccessful) {
                    val nickName = response.body()?.result?.nickname
                    binding.tvUsername.text = "${nickName}"
                } else {
                }

            }

            override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {
                Log.e("Account", "사용자 정보 요청 실패: ${t.message}")
            }
        })

        // UI 업데이트
        updateUI(clothList)


        binding.btnSave.setOnClickListener{
            // POST 요청을 위한 데이터 처리
            handleApiRequest(clothIDList)
        }

        return binding.root
    }


    override fun dismiss() {
        super.dismiss()
    }

    interface OnBottomSheetDismissListener {
        fun onBottomSheetDismissed()
        fun onCodiApiSuccess(requestId: Int)
    }

    // UI 업데이트
    private fun updateUI(clothList: MutableList<ClothRequestDesDTO>) {
        // clothList를 순회하며 clothKindId에 따라 TextView 업데이트
        for (cloth in clothList) {
            when (cloth.clothKindId) {
                1 -> binding.tvOuter.text = cloth.description // OUTER (1)
                2 -> binding.tvTop.text = cloth.description   // TOP (2)
                3 -> binding.tvBottom.text = cloth.description
                4 -> binding.tvShoes.text = cloth.description
                5 -> binding.tvBag.text = cloth.description
                6 -> binding.tvOther.text = cloth.description
            }
        }
    }


    // API 요청 처리
    private fun handleApiRequest(clothIDList: MutableList<ClothIdResponse>) {
        // 여기서 clothIDList를 사용하여 API 요청을 보낼 수 있음
        // 예를 들어 Retrofit을 사용하여 POST 요청을 보낼 수 있음

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate: String = dateFormat.format(Date())
        val inputText = binding.addInfo.text.toString()

        sendCodiResponse(requestId, 1, inputText, todayDate, "", clothIDList)
    }


    fun sendCodiResponse(requestId: Int, userId: Int, body: String, date: String, imageUrl: String, clothIds: List<ClothIdResponse>) {
        val request = CodiResponseRequest(
            userId = userId,
            body = body,
            date = date,
            imageUrl = imageUrl,
            clothRequestDTOList = clothIds
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.createService(RequestService::class.java).postCodiResponse(requestId, request)
                if (response.isSuccessful) {
                    println("요청 성공!")
                    dismiss()
                } else {
                    println("요청 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("네트워크 오류: ${e.message}")
            }
        }
    }

    companion object {
        const val TAG = "CodiBottomModalSheet"

        fun newInstance(clothList: MutableList<ClothRequestDesDTO>, clothIDList: MutableList<ClothIdResponse>, requestID: Int): CodiBottomSheetFragment {
            return CodiBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("CLOTH_LIST", ArrayList(clothList))
                    putParcelableArrayList("CLOTH_ID_LIST", ArrayList(clothIDList))
                    putInt("REQUEST_ID", requestID)
                }
            }
        }

    }
}
