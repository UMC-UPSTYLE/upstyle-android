package com.umc.upstyle

import Item_result
import Item_search
import RecyclerAdapter_Search
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentSearchFilterBinding
import retrofit2.Call

class SearchFilterFragment : Fragment(R.layout.fragment_search_filter) {

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    private var category: String? = null

    private var filteredItems: List<Item_search> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryfilterbtn.setOnClickListener {
            SharedPreferencesUtils.clearData(requireContext())
            findNavController().navigate(R.id.searchCategoryFragment) }
        binding.subcategoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchSubcategoryFragment) }
        binding.fitsizefilterbtn.setOnClickListener { findNavController().navigate(R.id.searchFitSizeFragment) }
        binding.colorfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchColorFragment) }

        val preferences = requireActivity().getSharedPreferences("FilterPrefsForFourFragments", Context.MODE_PRIVATE)

        val userId = 1
        val kindId = preferences.getInt("kindId", -1).takeIf { it != -1 }
        val categoryId = preferences.getInt("categoryId", -1).takeIf { it != -1 }
        val fitId = preferences.getInt("fitId", -1).takeIf { it != -1 }
        val colorId = preferences.getString("colorId", null)?.split(",")?.mapNotNull { it.toIntOrNull() }
        loadItemsFromApi(userId, kindId, categoryId, fitId, colorId)

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

    private fun setupRecyclerView(items: List<Item_search>) {
        binding.tvResultCount.text = items.size.toString()

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        if (items.isEmpty()) {
            Log.w("RecyclerView", "No items to display!") // ✅ 로그 추가
        }

        binding.recyclerView.adapter = RecyclerAdapter_Search(items) { selectedItem ->
            navigateToSearchFilterOotdFragment(selectedItem)
        }
    }

    private fun loadItemsFromApi(
        userId: Int,
        kindId: Int?,
        categoryId: Int?,
        fitId: Int?,
        colorId: List<Int>?
    ) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        Log.d("API_REQUEST", "Fetching items for kindId: $kindId")

        apiService.getClosetByCategory(userId, kindId, categoryId, colorId, fitId).enqueue(object : retrofit2.Callback<ClosetCategoryResponse> {
            override fun onResponse(call: Call<ClosetCategoryResponse>, response: retrofit2.Response<ClosetCategoryResponse>) {
                Log.d("API_RESPONSE", "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { closetResponse ->
                        Log.d("API_RESPONSE", "Closet Response: ${closetResponse.result.clothPreviewList}")

                        val filteredItems = closetResponse.result.clothPreviewList.mapNotNull { clothPreview ->
                            // null 또는 "null" 문자열 제거
                            val kindName = clothPreview.kindName?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
                            val categoryName = clothPreview.categoryName?.takeIf { it.lowercase() != "null" }
                            val fitName = clothPreview.fitName?.takeIf { it.lowercase() != "null" }
                            val colorName = clothPreview.colorName?.takeIf { it.lowercase() != "null" }

                            // 유효한 값만 리스트에 추가
                            val description = listOfNotNull(kindName, categoryName, fitName, colorName).joinToString(" ")

                            val imageUrl = clothPreview.ootd?.imageUrl ?: "https://example.com/default_image.jpg"

                            if (categoryName == null) {
                                Log.w("FILTER", "Skipping item with null categoryName")
                                return@mapNotNull null
                            }


                            // ✅ categoryId, fitId, colorId 추가
                            Item_search(
                                description = description,
                                imageUrl = imageUrl,
                                categoryId = clothPreview.categoryId,
                                fitId = clothPreview.fitId,
                                colorId = clothPreview.colorId
                            )
                        }

                        Log.d("FILTERED_ITEMS", "필터링된 아이템 개수: ${filteredItems.size}")

                        setupRecyclerView(filteredItems)
                    } ?: run {
                        Log.e("API_ERROR", "Response Body is Null")
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ClosetCategoryResponse>, t: Throwable) {
                Log.e("API_ERROR", "Failure: ${t.message}")
            }
        })
    }

    private fun navigateToSearchFilterOotdFragment(selectedItem: Item_search) {
        val bundle = Bundle().apply {
            putInt("kindId", -1)
            putString("imageUrl", selectedItem.imageUrl)
            putInt("categoryId", selectedItem.categoryId ?: -1)
            putInt("fitId", selectedItem.fitId ?: -1)
            putInt("colorId", selectedItem.colorId ?: -1)
        }

        Log.d("Navigation", "Navigating with bundle: $bundle")

        try {
            findNavController().navigate(R.id.searchFilterOotdFragment, bundle)
        } catch (e: Exception) {
            Log.e("Navigation Error", "Navigation failed: ${e.message}")
        }
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
