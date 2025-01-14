package com.umc.upstyle

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!! // Non-nullable로 사용하기 위한 접근자

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 초기화
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // OUTER 버튼 클릭
        binding.btnGoToOuter.setOnClickListener {
            navigateToSearchResult("OUTER")
        }

        // TOP 버튼 클릭
        binding.btnGoToTop.setOnClickListener {
            navigateToSearchResult("TOP")
        }

        // 다른 버튼들 클릭 (예시)
        binding.btnGoToBottom.setOnClickListener {
            navigateToSearchResult("BOTTOM")
        }

        binding.btnGoToShoes.setOnClickListener {
            navigateToSearchResult("SHOES")
        }

        binding.btnGoToBag.setOnClickListener {
            navigateToSearchResult("BAG")
        }

        binding.btnGoToOther.setOnClickListener {
            navigateToSearchResult("OTHER")
        }

    }

    // 프래그먼트로 문자열 전달 함수
    private fun navigateToSearchResult(category: String) {
        val fragment = SearchItemFragment().apply {
            arguments = Bundle().apply {
                putString("category", category) // 카테고리 문자열 전달
            }
        }

        // 현재는 그냥 프래그먼트 매니저
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // 뒤로가기 시 이전 프래그먼트로 돌아가도록 추가
            .commit()


        // navigation component로 수정하는 게 좋아보여서 일단 써둠
//            val navController = findNavController()
//            navController.navigate(R.id.action_searchFragment_to_closetResultFragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지: 뷰가 파괴되면 바인딩 해제
        _binding = null
    }
}