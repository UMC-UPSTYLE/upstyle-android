package com.umc.upstyle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.databinding.ActivityClosetBinding
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.data.viewmodel.CategoryViewModel
import com.umc.upstyle.data.viewmodel.ClosetViewModel
import com.umc.upstyle.data.viewmodel.RequestViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClosetFragment : Fragment() {
    private var _binding: ActivityClosetBinding? = null
    private val binding get() = _binding!!
    private var nickName: String = ""
    private var userId: Int = 0
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)
    private val closetViewModel: ClosetViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityClosetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    nickName = response.body()?.result?.nickname.toString()
                    binding.tvUsername.text = "${nickName}"
                    if (nickName != null) {
                        closetViewModel.username = nickName
                    }
                    Log.d("ClosetFragment", "사용자 닉네임 뷰모델 저장: ${closetViewModel.username}")
                } else {
                    binding.tvUsername.text = "알 수 없음"
                }

            }

            override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {
                Log.e("Account", "사용자 정보 요청 실패: ${t.message}")
            }
        })

        apiService.getMyCloset(null).enqueue(object : Callback<ClosetResponse> {
            override fun onResponse(call: Call<ClosetResponse>, response: Response<ClosetResponse>) {
                if (response.isSuccessful) {
                    val userName = response.body()?.result?.userName
                    binding.tvUsername.text = "${userName}"

                } else {
                    binding.tvUsername.text = "오류"
                }
            }

            override fun onFailure(call: Call<ClosetResponse>, t: Throwable) {
                binding.tvUsername.text = "API 실패"

            }
        })

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.mainFragment) // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "OUTER")
            findNavController().navigate(action)
        }

        binding.btnGoTop.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "TOP")
            findNavController().navigate(action)
        }

        binding.btnGoBottom.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "BOTTOM")
            findNavController().navigate(action)
        }

        binding.btnGoShoes.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "SHOES")
            findNavController().navigate(action)
        }

        binding.btnGoBag.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "BAG")
            findNavController().navigate(action)
        }

        binding.btnGoOther.setOnClickListener {
            val action = ClosetFragmentDirections.actionClosetFragmentToClosetItemFragment(category = "OTHER")
            findNavController().navigate(action)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 해제
    }
}
