package com.umc.upstyle

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 공통 클릭 이벤트 처리
        binding.btnGoToOuter.setOnClickListener { navigateToSearchItemFragment("OUTER") }
        binding.btnGoToTop.setOnClickListener { navigateToSearchItemFragment("TOP") }
        binding.btnGoToBottom.setOnClickListener { navigateToSearchItemFragment("BOTTOM") }
        binding.btnGoToShoes.setOnClickListener { navigateToSearchItemFragment("SHOES") }
        binding.btnGoToBag.setOnClickListener { navigateToSearchItemFragment("BAG") }
        binding.btnGoToOther.setOnClickListener { navigateToSearchItemFragment("OTHER") }
        binding.categoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.subcategoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.fitsizefilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.colorfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }


        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    // Navigation Component를 통한 전환 함수
    private fun navigateToSearchItemFragment(category: String) {
        val action = SearchFragmentDirections.actionSearchFragmentToSearchItemFragment(category)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지: 뷰가 파괴되면 바인딩 해제
        _binding = null
    }
}
