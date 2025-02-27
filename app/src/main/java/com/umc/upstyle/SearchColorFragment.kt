package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.SharedPreferencesUtils.getCategoryId
import com.umc.upstyle.databinding.FragmentSearchColorBinding

class SearchColorFragment : Fragment(R.layout.fragment_search_color) {

    private var _binding: FragmentSearchColorBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchColorBinding.bind(view)

        // ✅ SharedPreferences에서 값 불러오기
        filterViewModel.loadFromSharedPreferences(requireContext())

        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }

        // ✅ 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }

        // ✅ 색상 옵션 생성
        setupColorOptions()

        // ✅ SHOES 또는 OTHER 선택 시 colorTextView 비활성화 (회색 처리)
        if (filterViewModel.selectedCategory == "SHOES" || filterViewModel.selectedCategory == "OTHER") {
            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.fitsizeTextView.isClickable = false
            binding.fitsizeTextView.isEnabled = false
        }
//        else {
//            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_yellow))
//            binding.fitsizeTextView.isClickable = true
//            binding.fitsizeTextView.isEnabled = true
//        }

        // ✅ 완료 및 네비게이션 버튼 클릭 시
        binding.compOffButton.setOnClickListener { complete() }
        binding.categoryTextView.setOnClickListener { navigateToNextFragment("CATEGORY") }
        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.fitsizeTextView.setOnClickListener { navigateToNextFragment("FITSIZE") }
    }

    private fun setupColorOptions() {
        val colors = listOf(
            "블랙" to R.color.black, "실버" to R.color.silver, "화이트" to R.color.white,
            "그레이" to R.color.gray, "레드" to R.color.red, "버건디" to R.color.burgundy,
            "핑크" to R.color.pink, "오렌지" to R.color.orange, "아이보리" to R.color.ivory,
            "오트밀" to R.color.oatmeal, "옐로우" to R.color.yellow, "그린" to R.color.green,
            "카키" to R.color.khaki, "민트" to R.color.mint, "스카이블루" to R.color.skyblue,
            "블루" to R.color.blue, "네이비" to R.color.navy, "퍼플" to R.color.purple,
            "브라운" to R.color.brown, "카멜" to R.color.camel, "베이지" to R.color.beige,
            "연청" to R.color.light_denim, "중청" to R.color.denim, "흑청" to R.color.deep_denim,
            "기타색상" to R.color.othercolor
        )

        createButtons(binding.optionsLayout, colors) { selectedOption ->
            if (selectedOption == null) {
                // ✅ 선택 해제 시 ViewModel 초기화
                filterViewModel.selectedColor = null
                filterViewModel.colorId = null
                filterViewModel.saveToSharedPreferences(requireContext())
            } else {
                // ✅ 선택 시 ViewModel에 저장
                filterViewModel.selectedColor = selectedOption
                filterViewModel.colorId = getColorId(selectedOption)
                filterViewModel.saveToSharedPreferences(requireContext())
            }
        }

        // ✅ 이전에 선택한 색상이 있으면 버튼 상태 유지
        filterViewModel.selectedColor?.let { selectedColor ->
            binding.optionsLayout.children.forEach { view ->
                val button = view as TextView
                button.background = if (button.text == selectedColor) {
                    button.tag = true
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                } else {
                    button.tag = false
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                }
            }
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
            binding.compOffButton.isEnabled = true
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<Pair<String, Int>>, onClick: (String) -> Unit) {
        layout.removeAllViews()
        var selectedButton: TextView? = null

        options.forEach { option ->
            val button = TextView(requireContext()).apply {
                text = option.first
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)

                setOnClickListener {
                    if (this == selectedButton) {
                        // ✅ 현재 버튼이 이미 선택되어 있으면 초기화 및 SharedPreferences 삭제
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                        filterViewModel.selectedColor = null
                        filterViewModel.colorId = -1
                        filterViewModel.saveToSharedPreferences(requireContext())
                        selectedButton = null
                        onClick(null.toString())
                    } else {
                        // ✅ 다른 버튼이 선택되면 모든 버튼 초기화 및 새로운 버튼 선택
                        layout.children.forEach { view ->
                            (view as TextView).background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                        }
                        filterViewModel.selectedColor = null
                        filterViewModel.colorId = null
                        // ✅ 새로운 버튼 선택 및 SharedPreferences에 저장
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                        filterViewModel.selectedColor = option.first
                        filterViewModel.colorId = getColorId(option.first)
                        filterViewModel.saveToSharedPreferences(requireContext())
                        selectedButton = this
                        onClick(option.first)
                    }
                }
            }
            layout.addView(button, FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 16, 16, 16)
            })
        }
    }

    // ✅ 색상 이름에 따라 ID를 반환하는 함수
    private fun getColorId(color: String?): Int? {
        return when (color) {
            "블랙" -> 1 "실버" -> 2 "화이트" -> 3 "그레이" -> 4 "레드" -> 5
            "버건디" -> 6 "핑크" -> 7 "오렌지" -> 8 "아이보리" -> 9 "오트밀" -> 10
            "옐로우" -> 11 "그린" -> 12 "카키" -> 13 "민트" -> 14 "스카이블루" -> 15
            "블루" -> 16 "네이비" -> 17 "퍼플" -> 18 "브라운" -> 19 "카멜" -> 20
            "베이지" -> 21 "연청" -> 22 "중청" -> 23 "흑청" -> 24 "기타색상" -> 25 else -> null
        }
    }

    private fun navigateToNextFragment(type: String) {

        // ✅ SharedPreferences에 데이터 저장
        filterViewModel.saveToSharedPreferences(requireContext())

        // ✅ 프래그먼트 이동
        when (type) {
            "CATEGORY" -> findNavController().navigate(R.id.searchCategoryFragment)
            "SUBCATEGORY" -> findNavController().navigate(R.id.searchSubcategoryFragment)
            "FITSIZE" -> findNavController().navigate(R.id.searchFitSizeFragment)
        }


//        Toast.makeText(requireContext(), "${filterViewModel.selectedCategory} ${filterViewModel.selectedSubCategory} ${filterViewModel.selectedFitSize} ${filterViewModel.selectedColor}", Toast.LENGTH_SHORT).show()

    }

    private fun complete() {

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
