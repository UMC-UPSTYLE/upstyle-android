package com.umc.upstyle

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.databinding.FragmentSearchFilterBinding
import java.io.File

class SearchFilterFragment : Fragment(R.layout.fragment_search_filter) {

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    private var category: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.subcategoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchSubcategoryFragment) }
        binding.fitsizefilterbtn.setOnClickListener { findNavController().navigate(R.id.searchFitSizeFragment) }
        binding.colorfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchColorFragment) }

        category = arguments?.getString("CATEGORY")

        // RecyclerView 초기화 (null 방지)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = RecyclerAdapter(loadItemsFromPreferences())
        }
        updateFilterButtonState()

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        updateFilterButtonState()
    }

    // 각 필터 버튼 색상 변경
    private fun updateFilterButtonState() {
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.login_yellow)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.gray) // 기본 색상

        binding.categoryfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (!filterViewModel.selectedCategory.isNullOrEmpty()) yellowColor else defaultColor
        )

        binding.subcategoryfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (!filterViewModel.selectedSubCategory.isNullOrEmpty()) yellowColor else defaultColor
        )

        binding.fitsizefilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (!filterViewModel.selectedFitSize.isNullOrEmpty()) yellowColor else defaultColor
        )

        binding.colorfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (!filterViewModel.selectedColor.isNullOrEmpty()) yellowColor else defaultColor
        )
    }


    private fun loadItemsFromPreferences(): List<Item> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        // 해당 카테고리에 맞는 텍스트와 이미지 경로 가져오기
        val description = preferences.getString(category, "$category 정보 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)

        // 기본 샘플 데이터
        val items = mutableListOf(
            Item("후드집업 오버핏 네이비", "https://www.ocokorea.com//upload/images/product/148/148607/Product_1693647123947.jpg"),
            Item("후드집업 오버핏 블랙", "https://sitem.ssgcdn.com/70/26/15/item/1000363152670_i1_750.jpg"),
            Item("후드집업 오버핏 블랙", "https://m.likeygirl.kr/web/product/big/20231204_000027_LK.jpg")
        )

        // 저장된 데이터 추가
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty()) {
            val fileUri = Uri.fromFile(File(savedImagePath))
            items.add(0, Item(description, fileUri.toString()))
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(category: String): SearchFilterFragment {
            val fragment = SearchFilterFragment()
            val args = Bundle()
            args.putString("CATEGORY", category)
            fragment.arguments = args
            return fragment
        }
    }
}
