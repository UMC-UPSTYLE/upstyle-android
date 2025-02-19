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
import com.umc.upstyle.databinding.FragmentSearchFitSizeBinding
import com.umc.upstyle.databinding.FragmentSearchSubcategoryBinding
import com.umc.upstyle.utils.CategoryUtil

class SearchSubcategoryFragment : Fragment(R.layout.fragment_search_subcategory) {

    private var _binding: FragmentSearchSubcategoryBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchSubcategoryBinding.bind(view)

        filterViewModel.loadFromSharedPreferences(requireContext())

        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }

        setupSubcategoryOptions(filterViewModel.selectedCategory ?: "")

        if (filterViewModel.selectedCategory == "SHOES" || filterViewModel.selectedCategory == "OTHER") {
            binding.subcategoryTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.subcategoryTextView.isClickable = false
            binding.subcategoryTextView.isEnabled = false
        } else {
            binding.subcategoryTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_yellow))
            binding.subcategoryTextView.isClickable = true
            binding.subcategoryTextView.isEnabled = true
        }

        binding.compOffButton.setOnClickListener { complete() }
        binding.categoryTextView.setOnClickListener { navigateToNextFragment("CATEGORY") }
        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.colorTextView.setOnClickListener { navigateToNextFragment("COLOR") }
    }

    private fun setupSubcategoryOptions(category: String) {
        val options = getCategoryOptions(category)
        createButtons(binding.optionsLayout, options) { selectedOption ->
            filterViewModel.selectedSubCategory = selectedOption
            filterViewModel.categoryId = getCategoryId(selectedOption)
            filterViewModel.saveToSharedPreferences(requireContext())
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        }

        filterViewModel.selectedSubCategory?.let { selectedSubCategory ->
            binding.optionsLayout.children.forEach { view ->
                val button = view as TextView
                button.background = if (button.text == selectedSubCategory) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                }
            }
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
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
            val button = TextView(requireContext()).apply {
                text = option
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_gray)
                setOnClickListener {
                    layout.children.forEach { it.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background) }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                    onClick(option)
                }
            }
            layout.addView(button, FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 16, 16, 16)
            })
        }
    }

    private fun getCategoryId(subcategory: String?): Int? {
        val categoryIds: Pair<Int, Int>? = subcategory?.let { CategoryUtil.getCategoryIds(it) }
        return categoryIds?.first
    }

    private fun navigateToNextFragment(type: String) {

        filterViewModel.saveToSharedPreferences(requireContext())

        when (type) {
            "CATEGORY" -> findNavController().navigate(R.id.searchCategoryFragment)
            "FITSIZE" -> findNavController().navigate(R.id.searchFitSizeFragment)
            "COLOR" -> findNavController().navigate(R.id.searchColorFragment)
        }

        Toast.makeText(requireContext(), "${filterViewModel.selectedCategory} ${filterViewModel.selectedSubCategory} ${filterViewModel.selectedFitSize} ${filterViewModel.selectedColor}", Toast.LENGTH_SHORT).show()
    }

    private fun complete() {

        filterViewModel.saveToSharedPreferences(requireContext())

        findNavController().navigate(R.id.searchFilterFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
