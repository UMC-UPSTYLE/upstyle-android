package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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

        binding.backButton.setOnClickListener { findNavController().popBackStack(R.id.searchFragment, false) }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }



        setupCategoryOptions()

        binding.compOffButton.setOnClickListener { complete() }

        binding.subcategoryTextView.setOnClickListener { navigateToNextFragment("SUBCATEGORY") }
        binding.fitsizeTextView.setOnClickListener { navigateToNextFragment("FITSIZE") }
        binding.colorTextView.setOnClickListener { navigateToNextFragment("COLOR") }
    }

    private fun setupCategoryOptions() {
        val options = listOf("OUTER", "TOP", "BOTTOM", "SHOES", "BAG", "OTHER")
        createButtons(binding.optionsLayout, options) { selectedOption ->
            filterViewModel.selectedCategory = selectedOption
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
        if (filterViewModel.selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "카테고리를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

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
        findNavController().navigate(R.id.searchFilterFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
