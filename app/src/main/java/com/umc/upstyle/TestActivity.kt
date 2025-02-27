package com.umc.upstyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.umc.upstyle.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    // ViewBinding 객체 선언
    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityTestBinding.inflate(layoutInflater)

        // setContentView()로 ViewBinding의 root 레이아웃을 설정
        setContentView(binding.root)
    }
}
