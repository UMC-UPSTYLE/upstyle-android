package com.umc.upstyle

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    companion object {
        lateinit var instance: GlobalApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // ✅ 카카오 SDK 초기화
        KakaoSdk.init(this, "7b558a1bbfd23f2f42f6694d0575c8e3")

        // ✅ 파이어베이스 초기화 추가
        FirebaseApp.initializeApp(this)


        // Firebase App Check 설정
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}