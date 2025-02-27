package com.umc.upstyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.umc.upstyle.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

  // ViewBinding 객체 선언
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)

        // setContentView()로 ViewBinding root 레이아웃을 설정
        setContentView(binding.root)

        // NavHostFragment와 NavController 연결
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // BottomNavigationView와 NavController 연결
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

    }

}
