package com.umc.upstyle

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.user.UserApiClient
import com.umc.upstyle.data.model.AdditionalInfoRequestDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.Gender
import com.umc.upstyle.data.model.UserInfoDTO
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.databinding.ActivityBodyinfoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BodyinfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBodyinfoBinding

    private var isHeightEntered = false
    private var isWeightEntered = false
    private var isGenderSelected = false
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBodyinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etBodyinfoHeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isHeightEntered = !s.isNullOrEmpty()
                updateStartButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etBodyinfoWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isWeightEntered = !s.isNullOrEmpty()
                updateStartButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnBodyinfoMale.setOnClickListener {
            isGenderSelected = true
            selectedGender = "MALE"
            updateGenderUI(true)
        }

        binding.btnBodyinfoFemale.setOnClickListener {
            isGenderSelected = true
            selectedGender = "FEMALE"
            updateGenderUI(false)
        }

        binding.btBodyinfoStart.setOnClickListener {
            sendUserInfoToServer()
        }
    }

    private fun updateGenderUI(isMale: Boolean) {
        binding.btnBodyinfoMale.isSelected = isMale
        binding.btnBodyinfoFemale.isSelected = !isMale
        binding.btnBodyinfoMale.setBackgroundResource(
            if (isMale) R.drawable.button_background_gender_checked else R.drawable.button_background_gender_unchecked
        )
        binding.btnBodyinfoFemale.setBackgroundResource(
            if (!isMale) R.drawable.button_background_gender_checked else R.drawable.button_background_gender_unchecked
        )
        updateStartButtonState()
    }

    private fun updateStartButtonState() {
        val isButtonEnabled = isHeightEntered && isWeightEntered && isGenderSelected
        binding.btBodyinfoStart.isEnabled = isButtonEnabled
        binding.btBodyinfoStart.isClickable = isButtonEnabled
    }

    private fun sendUserInfoToServer() {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)

        if (jwtToken.isNullOrEmpty()) {
            Log.e("UserInfo", "❌ JWT 없음: 로그인이 필요합니다.")
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            logoutAndRedirectToLogin()
            return
        }

        // 키, 몸무게, 성별 기입
        val height = binding.etBodyinfoHeight.text.toString().toDoubleOrNull() ?: 0.0
        val weight = binding.etBodyinfoWeight.text.toString().toDoubleOrNull() ?: 0.0
        val gender = try {
            Gender.valueOf(selectedGender ?: "NONE")
        } catch (e: IllegalArgumentException) {
            Gender.NONE
        }

        // ✅ 카카오 사용자 정보 요청 (비동기)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("UserInfo", "❌ 사용자 정보 요청 실패: $error")
                sendAdditionalInfoToServer(jwtToken, "사용자", gender, height, weight) // 기본 닉네임 사용
            } else if (user != null) {
                val kakaoAccount = user.kakaoAccount
                val nickname = kakaoAccount?.profile?.nickname ?: "사용자"

                Log.d("UserInfo", "✅ 카카오 사용자 정보: 닉네임=$nickname")

                // ✅ 사용자 정보를 백엔드로 전송
                sendAdditionalInfoToServer(jwtToken, nickname, gender, height, weight)
            }
        }
    }

    private fun sendAdditionalInfoToServer(jwtToken: String, nickname: String, gender: Gender, height: Double, weight: Double) {
        val request = AdditionalInfoRequestDTO(nickname, gender, height, weight)
        val userApiService = RetrofitClient.createService(UserApiService::class.java)

        Log.d("UserInfo", "🚀 추가 정보 요청: JWT=$jwtToken, Request=$request")

        userApiService.addAdditionalInfo("Bearer $jwtToken", request)
            .enqueue(object : Callback<ApiResponse<UserInfoDTO>> {
                override fun onResponse(call: Call<ApiResponse<UserInfoDTO>>, response: Response<ApiResponse<UserInfoDTO>>) {
                    if (response.isSuccessful) {
                        Log.d("UserInfo", "✅ 추가 정보 저장 성공: ${response.body()}")
                        navigateToMainActivity()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("UserInfo", "❌ 추가 정보 저장 실패: $errorBody")
                        if (response.code() == 400) {
                            Toast.makeText(this@BodyinfoActivity, "잘못된 요청입니다.", Toast.LENGTH_SHORT).show()
                        } else if (response.code() == 401) {
                            Log.e("UserInfo", "❌ JWT 만료: 다시 로그인 필요")
                            logoutAndRedirectToLogin()
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UserInfoDTO>>, t: Throwable) {
                    Log.e("UserInfo", "❌ API 요청 실패: ${t.message}")
                }
            })
    }


    private fun logoutAndRedirectToLogin() {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        sharedPref.edit().remove("jwt_token").apply()

        Log.d("UserInfo", "🚨 JWT 삭제 및 로그인 화면 이동")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}