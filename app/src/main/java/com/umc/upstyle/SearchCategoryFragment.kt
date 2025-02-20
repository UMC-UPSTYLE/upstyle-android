package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.databinding.FragmentSearchCategoryBinding

class SearchCategoryFragment : Fragment(R.layout.fragment_search_category) {

    private var _binding: FragmentSearchCategoryBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchCategoryBinding.bind(view)

        // ✅ SharedPreferences에서 값 불러오기
        filterViewModel.loadFromSharedPreferences(requireContext())

        // ✅ UI 초기화
        setupCategoryOptions()

        // ✅ 뒤로가기 버튼
        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }

        // ✅ 완료 버튼 클릭 시
        binding.compOffButton.setOnClickListener { complete() }

        // ✅ 네비게이션
        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.fitsizeTextView.setOnClickListener { navigateToNextFragment("FITSIZE") }
        binding.colorTextView.setOnClickListener { navigateToNextFragment("COLOR") }
    }

    private fun setupCategoryOptions() {
        val options = listOf("OUTER", "TOP", "BOTTOM", "SHOES", "BAG", "OTHER")
        createButtons(binding.optionsLayout, options) { selectedOption ->
            filterViewModel.selectedCategory = selectedOption
            filterViewModel.kindId = getKindId(selectedOption)
            filterViewModel.saveToSharedPreferences(requireContext())
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        }

        // ✅ 이전에 선택한 카테고리가 있으면 버튼 상태 유지
        filterViewModel.selectedCategory?.let { selectedCategory ->
            binding.optionsLayout.children.forEach { view ->
                val button = view as TextView
                button.background = if (button.text == selectedCategory) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                }
            }
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<String>, onClick: (String?) -> Unit) {
        layout.removeAllViews()
        options.forEach { option ->
            val button = TextView(requireContext()).apply {
                text = option
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)

                var isSelected = false  // 버튼이 선택되었는지 여부를 확인하기 위한 변수

                setOnClickListener {
                    if (isSelected) {
                        // ✅ 이미 선택된 버튼을 다시 클릭하면 선택 해제 및 SharedPreferences 초기화
                        layout.children.forEach { it.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray) }
                        filterViewModel.clearSharedPreferences(requireContext())  // SharedPreferences 초기화
                        filterViewModel.selectedCategory = null
                        filterViewModel.kindId = null
                        isSelected = false
                        onClick(null)  // 선택 해제 시 null 전달
                    } else {
                        // ✅ 새로운 버튼 클릭 시 SharedPreferences 초기화 후 새로운 정보 저장
                        layout.children.forEach { it.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray) }
                        filterViewModel.clearSharedPreferences(requireContext())  // SharedPreferences 초기화
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                        filterViewModel.selectedCategory = option
                        filterViewModel.kindId = getKindId(option)
                        filterViewModel.saveToSharedPreferences(requireContext())
                        isSelected = true
                        onClick(option)
                    }
                }
            }
            layout.addView(button, FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 16, 16, 16)
            })
        }
    }


    private fun getKindId(category: String?): Int? {
        return when (category) {
            "OUTER" -> 1
            "TOP" -> 2
            "BOTTOM" -> 3
            "SHOES" -> 4
            "BAG" -> 5
            "OTHER" -> 6
            else -> null
        }
    }

    private fun navigateToNextFragment(type: String) {
        if (filterViewModel.selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ SharedPreferences에 데이터 저장
        filterViewModel.saveToSharedPreferences(requireContext())

        // ✅ 프래그먼트 이동
        when (type) {
            "SUBCATEGORY" -> findNavController().navigate(R.id.searchSubcategoryFragment)
            "FITSIZE" -> findNavController().navigate(R.id.searchFitSizeFragment)
            "COLOR" -> findNavController().navigate(R.id.searchColorFragment)
        }

        Toast.makeText(requireContext(), "${filterViewModel.selectedCategory} ${filterViewModel.selectedSubCategory} ${filterViewModel.selectedFitSize} ${filterViewModel.selectedColor}", Toast.LENGTH_SHORT).show()
    }

    private fun complete() {
        if (filterViewModel.selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "찾고자하는 의류 분류를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ SharedPreferences에 데이터 저장
        filterViewModel.saveToSharedPreferences(requireContext())

        // ✅ 네비게이션 수행
        findNavController().navigate(R.id.searchFilterFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}