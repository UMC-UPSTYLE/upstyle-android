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
import com.umc.upstyle.SharedPreferencesUtils.getCategoryId
import com.umc.upstyle.SharedPreferencesUtils.getColorId
import com.umc.upstyle.SharedPreferencesUtils.getFitId
import com.umc.upstyle.databinding.FragmentSearchFitSizeBinding

class SearchFitSizeFragment : Fragment(R.layout.fragment_search_fit_size) {

    private var _binding: FragmentSearchFitSizeBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchFitSizeBinding.bind(view)

        // ✅ SharedPreferences에서 값 불러오기
        filterViewModel.loadFromSharedPreferences(requireContext())

        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }

        // ✅ 핏 사이즈 옵션 생성
        setupCategoryOptions()

        // ✅ SHOES 또는 OTHER 선택 시 fitsizeTextView를 비활성화 (회색 처리)
        if (filterViewModel.selectedCategory == "SHOES" || filterViewModel.selectedCategory == "OTHER") {
            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.fitsizeTextView.isClickable = false
            binding.fitsizeTextView.isEnabled = false
        } else {
            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_yellow))
            binding.fitsizeTextView.isClickable = true
            binding.fitsizeTextView.isEnabled = true
        }

        // ✅ 완료 및 네비게이션 버튼 클릭 시
        binding.compOffButton.setOnClickListener { complete() }
        binding.categoryTextView.setOnClickListener { navigateToNextFragment("CATEGORY") }
        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.colorTextView.setOnClickListener { navigateToNextFragment("COLOR") }
    }

    private fun setupCategoryOptions() {
        val options = listOf("슬림", "레귤러", "오버핏")
        createButtons(binding.optionsLayout, options) { selectedOption ->
            if (selectedOption == null) {
                filterViewModel.selectedFitSize = null
                filterViewModel.fitId = null
                filterViewModel.saveToSharedPreferences(requireContext()) // ✅ 바로 SharedPreferences에 저장
            } else {
                filterViewModel.selectedFitSize = selectedOption
                filterViewModel.fitId = getFitId(selectedOption) // ✅ 핏 사이즈 ID를 숫자로 저장
                filterViewModel.saveToSharedPreferences(requireContext()) // ✅ 바로 SharedPreferences에 저장
            }
        }

        // ✅ 이전에 선택한 핏 사이즈가 있으면 버튼 상태 유지
        filterViewModel.selectedFitSize?.let { selectedFitSize ->
            binding.optionsLayout.children.forEach { view ->
                val button = view as TextView
                button.background = if (button.text == selectedFitSize) {
                    button.tag = true
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                } else {
                    button.tag = false
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                }
            }
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        }
        binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        binding.compOffButton.isEnabled = true
    }

    private fun createButtons(layout: FlexboxLayout, options: List<String>, onClick: (String) -> Unit) {
        layout.removeAllViews()
        var selectedButton: TextView? = null  // 현재 선택된 버튼을 저장

        options.forEach { option ->
            val button = TextView(requireContext()).apply {
                text = option
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)

                setOnClickListener {
                    if (this == selectedButton) {
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                        filterViewModel.selectedFitSize = null
                        filterViewModel.fitId = -1
                        filterViewModel.saveToSharedPreferences(requireContext())
                        selectedButton = null
                        onClick(null.toString())

                    } else {
                        // ✅ 다른 버튼이 선택되면 모든 버튼 초기화 및 새로운 버튼 선택
                        layout.children.forEach { view ->
                            (view as TextView).background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                        }
                        filterViewModel.selectedFitSize = null
                        filterViewModel.fitId = null
                        // ✅ 새로운 버튼 선택 및 SharedPreferences에 저장
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                        filterViewModel.selectedFitSize = option
                        filterViewModel.fitId = getFitId(option)
                        filterViewModel.saveToSharedPreferences(requireContext())
                        selectedButton = this
                        onClick(option)
                    }
                }
            }
            layout.addView(button, FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 16, 16, 16)
            })
        }
    }

    // ✅ 핏 사이즈 이름에 따라 ID를 반환하는 함수
    private fun getFitId(fitSize: String?): Int? {
        return when (fitSize) {
            "슬림" -> 1
            "레귤러" -> 2
            "오버핏" -> 3
            else -> null
        }
    }

    private fun navigateToNextFragment(type: String) {
        // ✅ SharedPreferences에 데이터 저장
        filterViewModel.saveToSharedPreferences(requireContext())

        // ✅ 프래그먼트 이동
        when (type) {
            "CATEGORY" -> findNavController().navigate(R.id.searchCategoryFragment)
            "SUBCATEGORY" -> findNavController().navigate(R.id.searchSubcategoryFragment)
            "COLOR" -> findNavController().navigate(R.id.searchColorFragment)
        }

        Toast.makeText(requireContext(), "${filterViewModel.selectedCategory} ${filterViewModel.selectedSubCategory} ${filterViewModel.selectedFitSize} ${filterViewModel.selectedColor}", Toast.LENGTH_SHORT).show()
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
