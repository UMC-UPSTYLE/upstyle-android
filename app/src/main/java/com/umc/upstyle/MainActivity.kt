package com.umc.upstyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.umc.upstyle.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

  // ViewBinding 객체 선언
    lateinit var binding: ActivityMainBinding
  
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)

        // setContentView()로 ViewBinding root 레이아웃을 설정
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation_view)

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            loadFragment(BookmarkFragment())  // 기본으로 BookmarkFragment를 로드
        }

        // BottomNavigationView 클릭 이벤트 처리
        setBottomNavigationView(bottomNavigationView)
    }

    // BottomNavigationView에 클릭 리스너를 설정하는 함수
    private fun setBottomNavigationView(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> {
                    loadFragment(MyHomeFragment())
                    true
                }
                R.id.item_search -> {
                    loadFragment(SearchFragment())  // SearchFragment 로드
                    true
                }
                R.id.item_bookmark -> {
                    loadFragment(BookmarkFragment())  // BookmarkFragment 로드
                    true
                }
                R.id.item_chat -> {
                    loadFragment(ChatFragment())  // ChatFragment 로드
                    true
                }
                R.id.item_account -> {
                    loadFragment(AccountFragment())  // AccountFragment 로드
                    true
                }
                else -> false
            }
        }
    }

    // 프래그먼트를 동적으로 로드하는 함수
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)  // fragment_container에 프래그먼트 교체
            .commit()
    }
}
