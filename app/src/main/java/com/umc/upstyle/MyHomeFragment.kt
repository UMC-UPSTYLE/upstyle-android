package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.umc.upstyle.databinding.ActivityMyhomeBinding
import java.util.Calendar

class MyHomeFragment : Fragment() {
    private var _binding: ActivityMyhomeBinding? = null
    private val binding get() = _binding!! // Non-nullable binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityMyhomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 연도와 월 가져오기
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // MONTH는 0부터 시작하므로 +1 필요

        // TextView에 연도와 월 설정
        binding.calendarMonth.text = "${year}년 ${month}월"

        // closet섹션으로 이동 (뒤로 가기 버튼 없이)
        binding.closetSection.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, ClosetFragment())
            transaction.addToBackStack(null) // 백스택에 추가
            transaction.commit()
        }


        // todayOOTDSection 버튼 클릭 이벤트 설정
        binding.todayOOTDSection.setOnClickListener {
            // TodayOotdFragment로 이동
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, TodayOotdFragment())
            transaction.addToBackStack(null) // 뒤로 가기 버튼으로 돌아가기 가능
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Binding 객체 해제
    }
}
