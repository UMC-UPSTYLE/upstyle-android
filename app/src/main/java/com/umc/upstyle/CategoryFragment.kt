package com.umc.upstyle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.ActivityCategoryBinding
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.utils.CategoryUtil

class CategoryFragment : Fragment(R.layout.activity_category) {

    private var _binding: ActivityCategoryBinding? = null
    private val binding get() = _binding!!

    private var selectedSubCategory: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivityCategoryBinding.bind(view)  // 바인딩 초기화

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        val selectedCategory = arguments?.getString("CATEGORY")
        Log.d("CategoryFragment", "Selected Category: $selectedCategory")

        if (selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
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
        selectedSubCategory?.let { subCategory ->
            val (categoryId, kindId) = CategoryUtil.getCategoryIds(subCategory) ?: Pair(-1, -1)

            val bundle = Bundle().apply {
                putString("CATEGORY", selectedCategory)
                putString("SUB_CATEGORY", subCategory)

                putInt("KIND_ID", kindId)
                putInt("CATEGORY_ID", categoryId)
            }

            Log.d("CategoryFragment", "Navigating with CATEGORY=$selectedCategory, SUB_CATEGORY=$subCategory, ID=$categoryId, KIND_ID=$kindId")

            when (selectedCategory) {
                "OUTER", "TOP", "BOTTOM" -> findNavController().navigate(R.id.action_categoryFragment_to_fitFragment, bundle)
                "SHOES", "OTHER" -> findNavController().navigate(R.id.action_categoryFragment_to_colorFragment, bundle)
                "BAG" -> findNavController().navigate(R.id.action_categoryFragment_to_sizeFragment, bundle)
                else -> Toast.makeText(requireContext(), "올바른 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(requireContext(), "서브 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()

    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}