package com.umc.upstyle

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.user.UserApiClient
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.databinding.FragmentAccountBinding
import com.umc.upstyle.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountFragment : Fragment(R.layout.fragment_account) {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.myProfileBtn.setOnClickListener { findNavController().navigate(R.id.myProfileFragment) }
        binding.privacyPolicy.setOnClickListener {
            val bundle = Bundle().apply { putString("URL", "https://judicious-quiver-042.notion.site/1841ce3fbf8380acb266cd73c4ab72ed") } // URL을 전달
            findNavController().navigate(R.id.webViewFragment, bundle)
        }
        binding.termsOfService.setOnClickListener {
            val bundle = Bundle().apply { putString("URL", "https://judicious-quiver-042.notion.site/1841ce3fbf8380d68eeedc8911eb1af0") } // URL을 전달
            findNavController().navigate(R.id.webViewFragment, bundle)
        }

        binding.logoutBtn.setOnClickListener { showLoadItemPopupDialog() }

        fetchUserNickname()
    }

    // JWT로 닉네임, 이메일 가져오기
    private fun fetchUserNickname() {
        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)

        userApiService.getUserInfo("Bearer $jwtToken")
            .enqueue(object : Callback<ApiResponse<AccountInfoDTO>> {
                override fun onResponse(
                    call: Call<ApiResponse<AccountInfoDTO>>,
                    response: Response<ApiResponse<AccountInfoDTO>>
                ) {
                    binding.userNickname.text = response.body()?.result?.nickname ?: "닉네임을 불러올 수 없음"
                    binding.userEmail.text = response.body()?.result?.email ?: "이메일을 불러올 수 없음"
                }

                override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {}
            })

    }


    private fun showLoadItemPopupDialog() {
        val dialog = LogoutPopupDialog(
            requireContext(),
            onNoClicked = {

            },
            onYesClicked = {
                logout()
            }
        )
        dialog.show()
    }

    private fun logout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                unlinkKakaoAccount() // 로그아웃 실패 시, 강제 연결 해제 (unlink)
            } else {
                clearJwtAndNavigateToLogin() // 로그아웃 후 JWT 삭제 및 로그인 화면 이동
            }
        }
    }

    // 카카오 계정 강제 연결 해제
    private fun unlinkKakaoAccount() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
            } else {
                clearJwtAndNavigateToLogin() // 계정 연결 해제 후 JWT 삭제 및 로그인 화면 이동
            }
        }
    }

    // JWT 삭제 후 로그인 화면으로 이동
    private fun clearJwtAndNavigateToLogin() {
        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        sharedPref.edit().remove("jwt_token").apply()

        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // 현재 액티비티 종료
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지: 뷰가 파괴되면 바인딩 해제
        _binding = null
    }

}