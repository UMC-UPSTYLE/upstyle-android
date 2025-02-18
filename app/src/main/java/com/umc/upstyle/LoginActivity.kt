package com.umc.upstyle

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.umc.upstyle.data.network.AuthApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authApiService = RetrofitClient.createService(AuthApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // 액션바 숨기기
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)
        val kakaoAccessToken = sharedPref.getString("kakao_access_token", null) // ✅ Access Token 확인

        // ✅ 로그 추가해서 SharedPreferences 상태 확인
        Log.d("Login", "📌 앱 실행 시 SharedPreferences 상태")
        Log.d("Login", "📌 jwt_token: $jwtToken")
        Log.d("Login", "📌 kakao_access_token: $kakaoAccessToken")


        if (!jwtToken.isNullOrEmpty() && !kakaoAccessToken.isNullOrEmpty()) {
            Log.d("Login", "✅ JWT 및 Access Token 존재 → Access Token 검증 시작")

            // ✅ Access Token이 유효한지 먼저 확인 (만료되었을 경우만 갱신)
            UserApiClient.instance.accessTokenInfo { token, error ->
                if (error != null) {
                    Log.e("Login", "❌ Access Token 유효성 검사 실패: ${error.message}")
                    Log.d("Login", "🔄 Access Token 만료 → 갱신 시도")
                    checkAndRefreshAccessToken()
                } else {
                    Log.d("Login", "✅ Access Token 유효함 → 자동 로그인 진행")
                    navigateToMainActivity()
                }
            }
        } else {
            Log.d("Login", "❌ 자동 로그인 불가: JWT & Access Token 없음 또는 로그아웃됨")
        }


        binding.btKakaoLogin.setOnClickListener {
            logoutAndStartKakaoLogin()
        }
    }


    private fun checkAndRefreshAccessToken() {
        UserApiClient.instance.accessTokenInfo { token, error ->
            if (error != null) {
                Log.e("Token", "❌ Access Token 확인 실패: ${error.message}")
                Log.d("Token", "🔄 Access Token 만료됨 → 새 로그인 필요")
                logoutAndStartKakaoLogin() // ✅ 기존 Access Token 삭제 후 새 로그인
            } else if (token != null) {
                Log.d("Token", "✅ Access Token 유효함, 만료까지 남은 시간: ${token.expiresIn}초")

                // ✅ 새로운 Access Token을 저장하여 최신 상태 유지
                val storedAccessToken = getStoredAccessToken()
                if (storedAccessToken != null) {
                    saveAccessToken(storedAccessToken)
                } else {
                    Log.e("Token", "❌ 저장된 Access Token이 없음")
                }
            }
        }
    }


    private fun getStoredAccessToken(): String? {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        return sharedPref.getString("kakao_access_token", null) // ✅ 저장된 Access Token 반환
    }



    // 기존 JWT 삭제 후 카카오 로그인 시작
    private fun logoutAndStartKakaoLogin() {
        val sharedPref = getSharedPreferences("Auth", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()  // ✅ 모든 저장된 값 삭제
        editor.commit()


        UserApiClient.instance.logout { error ->
            startKakaoLogin()
        }
    }

    // ✅ 카카오 로그인 시작
    private fun startKakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                handleKakaoLoginResponse(token, error)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    Log.e("Login", "❌ 카카오 로그인 실패: ${error.message}")
                    navigateToLogin() // ✅ 로그인 실패 시 로그인 화면으로 이동
                } else {
                    handleKakaoLoginResponse(token, null)
                }
            }
        }
    }

    private fun handleKakaoLoginResponse(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Log.e("Login", "카카오 로그인 실패: $error")
        } else if (token != null) {
            Log.d("Login", "카카오 로그인 성공: ${token.accessToken}")
            saveAccessToken(token.accessToken) // ✅ 카카오 Access Token 저장
            requestUserInfo(token.accessToken) // ✅ 사용자 정보 요청 후 백엔드로 전송
        }
    }

    // Access Token 저장
    private fun saveAccessToken(accessToken: String) {
        getSharedPreferences("Auth", MODE_PRIVATE)
            .edit()
            .putString("kakao_access_token", accessToken)
            .apply()
    }

    // ✅ 사용자 정보 요청 (닉네임 & 이메일)
    private fun requestUserInfo(accessToken: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("Login", "사용자 정보 요청 실패: $error")
                sendTokenToServer(accessToken) // 오류 시 Access Token만 전송
            } else if (user != null) {
                val kakaoAccount = user.kakaoAccount
                val email = kakaoAccount?.email
                val nickname = kakaoAccount?.profile?.nickname

                Log.d("Login", "카카오 사용자 정보: 닉네임=${nickname ?: "없음"}, 이메일=${email ?: "없음"}")

                sendTokenToServer(accessToken) // ✅ 사용자 정보와 함께 Access Token 전송
            }
        }
    }

    private fun sendTokenToServer(kakaoAccessToken: String) {
        Log.d("Login", "백엔드로 AccessToken 전송: $kakaoAccessToken")

        authApiService.loginWithKakao(kakaoAccessToken).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null) {
                    saveJwt(response.body()!!)
                    saveAccessToken(kakaoAccessToken)
                    navigateToBodyInfoActivity()
                } else {
                    Log.e("Login", "❌ JWT 요청 실패: ${response.errorBody()?.string()}")
                    navigateToLogin() // ✅ JWT 요청 실패 시 로그인 화면으로 이동
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Login", "❌ JWT 요청 에러: ${t.message}")
                navigateToLogin() // ✅ 네트워크 오류 발생 시 로그인 화면으로 이동
            }
        })
    }

    // ✅ 기존 JWT 삭제 후 새로운 JWT 저장
    private fun saveJwt(jwt: String) {
        getSharedPreferences("Auth", MODE_PRIVATE)
            .edit()
            .putString("jwt_token", jwt)
            .apply()

        Log.d("JWT", "✅ 새로운 JWT 저장 완료")
    }

    // ✅ 로그인 화면으로 이동하는 함수 추가
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("isLogout", true)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToBodyInfoActivity() {
        startActivity(Intent(this, BodyinfoActivity::class.java))
        finish()
    }
}
