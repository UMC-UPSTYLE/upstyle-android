package com.umc.upstyle

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.user.Constants.GENDER
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.Gender
import com.umc.upstyle.data.model.NicknameDTO
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.databinding.FragmentMyProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)

    private val existingNicknames = listOf("apple", "test") // 중복 검사 리스트

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyProfileBinding.bind(view)

        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)

        userApiService.getUserInfo("Bearer $jwtToken").enqueue(object :
            Callback<ApiResponse<AccountInfoDTO>> {
            override fun onResponse(call: Call<ApiResponse<AccountInfoDTO>>, response: Response<ApiResponse<AccountInfoDTO>>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()?.result
                    binding.userEmail.text = userInfo?.email ?: "이메일을 불러올 수 없음"
                    binding.nicknameTextView.text = userInfo?.nickname ?: "닉네임을 불러올 수 없음"
                    binding.userBodyinfo.text = userInfo?.let { "${it.height}cm, ${it.weight}kg" } ?: "체형정보를 불러올 수 없음"
                    binding.userGender.text = when (userInfo?.gender) {
                        Gender.MALE -> "남성"
                        Gender.FEMALE -> "여성"
                        else -> "성별을 불러올 수 없음"
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {
                Log.e("Account", "사용자 정보 요청 실패: ${t.message}")
            }
        })

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 뒤로 가기
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // 닉네임 수정 버튼 클릭 시 EditText 활성화
        binding.editNicknameButton.setOnClickListener { enableEditMode() }

        // 닉네임 입력 시 연필 아이콘 숨기기 및 유효성 검사 실행
        binding.nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateNickname(s.toString())
                binding.editNicknameButton.visibility = View.GONE
                binding.namechkimg.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 닉네임 입력 완료
        binding.nicknameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //saveNickname()
                updateNickname(jwtToken)
                disableEditMode()
                binding.namechkimg.visibility = View.GONE
                true
            } else {
                false
            }
        }

    }

    // 닉네임 유효성 검사 (입력하면 namechkimg를 보여줌)
    private fun validateNickname(nickname: String) {
        val nicknamePattern = "^[가-힣a-zA-Z0-9]+$".toRegex()

        when {
            nickname.isEmpty() -> {
                binding.errorTextView.text = ""
                binding.nicknameEditText.setTextColor(Color.WHITE)
                binding.namechkimg.visibility = View.GONE  // 빈 칸이면 숨김
            }
            nickname.length > 8 -> {
                binding.errorTextView.text = "최대 8자까지 입력 가능합니다."
                binding.errorTextView.setTextColor(Color.RED)
                binding.nicknameEditText.setTextColor(Color.RED)
                binding.namechkimg.setImageResource(R.drawable.ic_namechk_red)
                binding.namechkimg.visibility = View.VISIBLE
            }
            !nicknamePattern.matches(nickname) -> {
                binding.errorTextView.text = "한글, 영문, 숫자만 입력 가능합니다."
                binding.errorTextView.setTextColor(Color.RED)
                binding.nicknameEditText.setTextColor(Color.RED)
                binding.namechkimg.setImageResource(R.drawable.ic_namechk_red)
                binding.namechkimg.visibility = View.VISIBLE
            }
            existingNicknames.contains(nickname) -> {
                binding.errorTextView.text = "이미 사용 중인 닉네임입니다."
                binding.errorTextView.setTextColor(Color.RED)
                binding.nicknameEditText.setTextColor(Color.RED)
                binding.namechkimg.setImageResource(R.drawable.ic_namechk_red)
                binding.namechkimg.visibility = View.VISIBLE
            }
            else -> {
                binding.errorTextView.text = ""
                binding.nicknameEditText.setTextColor(Color.WHITE)
                binding.namechkimg.setImageResource(R.drawable.ic_namechk_white)
                binding.namechkimg.visibility = View.VISIBLE
            }
        }
        if (nickname.isNotEmpty() && nickname.length <= 8 && nicknamePattern.matches(nickname) && !existingNicknames.contains(nickname)) {
            binding.namechkimg.visibility = View.GONE
        }
    }

    // 닉네임 API 요청
    private fun updateNickname(jwtToken: String?) {
        val newNickname = binding.nicknameEditText.text.toString().trim()

        if (newNickname.isEmpty()) {
            Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = NicknameDTO(newNickname)

        jwtToken?.let {
            userApiService.updateNickname("Bearer $jwtToken", requestBody)
                .enqueue(object : Callback<ApiResponse<NicknameDTO>> {
                    override fun onResponse(
                        call: Call<ApiResponse<NicknameDTO>>,
                        response: Response<ApiResponse<NicknameDTO>>
                    ) {
                        if (response.isSuccessful) {
                            binding.nicknameTextView.text = newNickname
                            disableEditMode()
                            Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "닉네임 변경 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<NicknameDTO>>, t: Throwable) {
                        Toast.makeText(requireContext(), "서버 오류 발생: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // 닉네임 수정 모드 활성화
    private fun enableEditMode() {
        binding.nicknameTextView.visibility = View.GONE  // 기존 닉네임 숨김
        binding.nicknameEditText.visibility = View.VISIBLE  // 입력창 표시
        binding.nicknameEditText.setText(binding.nicknameTextView.text) // 기존 닉네임 설정
        binding.nicknameEditText.requestFocus() // 포커스 줌
        binding.editNicknameButton.visibility = View.GONE // 닉네임 수정 중에는 연필 버튼 숨김
    }

    // 닉네임 수정 모드 비활성화
    private fun disableEditMode() {
        binding.nicknameTextView.visibility = View.VISIBLE  // 기존 닉네임 표시
        binding.nicknameEditText.visibility = View.GONE  // 입력창 숨김
        binding.nicknameTextView.text = binding.nicknameEditText.text.toString() // 수정된 닉네임 반영
        binding.editNicknameButton.visibility = View.VISIBLE // 수정이 끝나면 연필 버튼 다시 표시
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}