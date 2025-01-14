package com.umc.upstyle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.umc.upstyle.databinding.ActivityCategoryBinding
import com.google.android.flexbox.FlexboxLayout

class CategoryFragment : Fragment(R.layout.activity_category) {

    private var _binding: ActivityCategoryBinding? = null
    private val binding get() = _binding!!

    private var selectedSubCategory: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivityCategoryBinding.bind(view)  // 바인딩 초기화

        // 삭제하기
        val test = binding.test
        test.setOnClickListener {
            val filterFragment = FilterFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, filterFragment) // fragment_container는 해당 Fragment를 포함할 컨테이너의 ID
                .addToBackStack(null) // 백 스택에 추가하여 뒤로 가기 가능
                .commit()
        }


        val goBackButton = binding.backButton
        goBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()  // 이전 화면으로 돌아가기
        }

        val selectedCategory = arguments?.getString("CATEGORY")
        if (selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        binding.mainTitleTextView.text = "$selectedCategory"

        setupCategoryOptions(selectedCategory)

        binding.nextButton.setOnClickListener {
            navigateToNextStep(selectedCategory)
        }
    }

    private fun setupCategoryOptions(selectedCategory: String) {
        val options = getCategoryOptions(selectedCategory)
        createButtons(binding.optionsLayout, options) { selectedOption ->
            selectedSubCategory = selectedOption
            binding.nextButton.setBackgroundResource(R.drawable.next_on)
        }
    }

    private fun getCategoryOptions(category: String): List<String> {
        return when (category) {
            "OUTER" -> listOf("레더", "무스탕", "가디건", "코트", "숏패딩", "롱패딩", "블레이저", "트러커 재킷", "후드집업", "트레이닝 재킷", "기타")
            "TOP" -> listOf("셔츠", "맨투맨", "블라우스", "니트", "후드티", "카라 티셔츠", "긴소매 티셔츠", "반소매 티셔츠", "민소매 티셔츠", "기타")
            "BOTTOM" -> listOf("숏팬츠", "데님팬츠", "코튼팬츠", "레깅스", "트레이닝", "미니스커트", "미디스커트", "롱스커트", "롱원피스", "미니원피스", "미디원피스", "기타")
            "SHOES" -> listOf("구두", "스니커즈", "스포츠화", "샌들/슬리퍼", "패딩/퍼 신발", "부츠/워커", "기타")
            "BAG" -> listOf("백팩", "크로스백", "숄더백", "핸드백", "웨이스트백", "기타")
            "OTHER" -> listOf("모자", "머플러", "액세서리", "시계", "벨트", "양말", "선글라스/안경", "기타")
            else -> emptyList()
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<String>, onClick: (String) -> Unit) {
        layout.removeAllViews()

        options.forEach { option ->
            val button = Button(requireContext()).apply {
                text = option
                setTypeface(null, android.graphics.Typeface.BOLD)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background)
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)

                setOnClickListener {
                    layout.children.forEach { (it as Button).background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_background)
                    }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                    onClick(option)
                }
            }

            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                100
            ).apply {
                setMargins(16, 16, 16, 16)
            }

            layout.addView(button, layoutParams)
        }
    }

    private fun navigateToNextStep(selectedCategory: String) {
        if (selectedSubCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "서브 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val nextFragment = when (selectedCategory) {
            "OUTER", "TOP", "BOTTOM" -> FitFragment().apply {
                arguments = Bundle().apply {
                    putString("CATEGORY", selectedCategory)
                    putString("SUB_CATEGORY", selectedSubCategory)
                }
            }
            "SHOES", "OTHER" -> ColorFragment().apply {
                arguments = Bundle().apply {
                    putString("CATEGORY", selectedCategory)
                    putString("SUB_CATEGORY", selectedSubCategory)
                }
            }
            "BAG" -> SizeFragment().apply { // SizeFragment로 수정 필요
                arguments = Bundle().apply {
                    putString("CATEGORY", selectedCategory)
                    putString("SUB_CATEGORY", selectedSubCategory)
                }
            }
            else -> {
                Toast.makeText(requireContext(), "올바른 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Fragment 전환 처리
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, nextFragment) // fragment_container는 FrameLayout ID
            .addToBackStack(null) // 뒤로 가기 지원
            .commit()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}
