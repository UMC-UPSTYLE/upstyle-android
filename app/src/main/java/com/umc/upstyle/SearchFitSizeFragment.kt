package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.databinding.FragmentSearchFitSizeBinding

class SearchFitSizeFragment : Fragment(R.layout.fragment_search_fit_size) {

    private var _binding: FragmentSearchFitSizeBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchFitSizeBinding.bind(view)

        if (filterViewModel.selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }

        setupCategoryOptions()

        // SHOES 또는 OTHER 선택 시 fitsizeTextView를 비활성화 (회색 처리)
        if (filterViewModel.selectedCategory == "SHOES" || filterViewModel.selectedCategory == "OTHER") {
            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            binding.fitsizeTextView.isClickable = false
            binding.fitsizeTextView.isEnabled = false
        } else {
            binding.fitsizeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_yellow))
            binding.fitsizeTextView.isClickable = true
            binding.fitsizeTextView.isEnabled = true
        }

        binding.compOffButton.setOnClickListener { complete() }

        binding.categoryTextView.setOnClickListener { navigateToNextFragment("CATEGORY") }
        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.colorTextView.setOnClickListener { navigateToNextFragment("COLOR") }
    }

    private fun setupCategoryOptions() {
        val options = listOf("슬림", "레귤러", "오버핏")
        createButtons(binding.optionsLayout, options) { selectedOption ->
            filterViewModel.selectedFitSize = selectedOption
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
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

    private fun navigateToNextFragment(type: String) {
        when (type) {
            "CATEGORY" -> findNavController().navigate(R.id.searchCategoryFragment)
            "SUBCATEGORY" -> findNavController().navigate(R.id.searchSubcategoryFragment)
            "COLOR" -> findNavController().navigate(R.id.searchColorFragment)
        }

        Toast.makeText(requireContext(), "${filterViewModel.selectedCategory} ${filterViewModel.selectedSubCategory} ${filterViewModel.selectedFitSize} ${filterViewModel.selectedColor}", Toast.LENGTH_SHORT).show()
    }

    private fun complete() {
        if (filterViewModel.selectedFitSize.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "핏/사이즈를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(R.id.searchFilterFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}