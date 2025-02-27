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
import com.umc.upstyle.data.model.ClothesCategoryResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentSearchFilterBinding
import retrofit2.Call
import retrofit2.Response

class SearchFilterFragment : Fragment(R.layout.fragment_search_filter) {

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding get() = _binding!!

    private val filterViewModel: FilterViewModel by activityViewModels()

    private var filteredItems: List<Item_search> = emptyList()

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

        val preferences = requireActivity().getSharedPreferences("FilterPrefsForFourFragments", Context.MODE_PRIVATE)

        val userId = 1
        val kindId = preferences.getInt("kindId", -1).takeIf { it != -1 }
        val categoryId = preferences.getInt("categoryId", -1).takeIf { it != -1 }
        val fitId = preferences.getInt("fitId", -1).takeIf { it != -1 }
        val colorId = preferences.getInt("colorId", -1).takeIf { it != -1 }?.let { listOf(it) }
        loadItemsFromApi(userId, kindId, categoryId, fitId, colorId)


        loadItemsFromApi(userId, kindId, categoryId, fitId, colorId)


        updateFilterButtonState()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        updateFilterButtonState()
    }

    private fun updateFilterButtonState() {
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.login_yellow)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.gray)

        binding.categoryfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (filterViewModel.kindId != null) yellowColor else defaultColor
        )

        binding.subcategoryfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (filterViewModel.categoryId != null) yellowColor else defaultColor
        )

        binding.fitsizefilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (filterViewModel.fitId != null) yellowColor else defaultColor
        )

        binding.colorfilterbtn.backgroundTintList = ColorStateList.valueOf(
            if (filterViewModel.colorId != null) yellowColor else defaultColor
        )
    }

    private fun setupRecyclerView(items: List<Item_search>) {
        binding.tvResultCount.text = items.size.toString()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
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

        apiService.getClothesByCategory(kindId, categoryId, colorId, fitId)
            .enqueue(object : retrofit2.Callback<ClothesCategoryResponse> {
                override fun onResponse(
                    call: Call<ClothesCategoryResponse>,
                    response: Response<ClothesCategoryResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { clothesResponse ->
                            filteredItems = clothesResponse.result.clothPreviewList?.mapNotNull { clothPreview ->
                                val description = listOfNotNull(
//                                    clothPreview.kindName?.takeIf { it.isNotBlank() },
                                    clothPreview.categoryName?.takeIf { it.isNotBlank() },
                                    clothPreview.fitName?.takeIf { it.isNotBlank() },
                                    clothPreview.colorName?.takeIf { it.isNotBlank() }
                                ).joinToString(" ")

                                Item_search(
                                    description = description,
                                    imageUrl = clothPreview.ootd?.imageUrl ?: "https://example.com/default_image.jpg",
                                    categoryId = clothPreview.categoryId,
                                    fitId = clothPreview.fitId,
                                    colorId = clothPreview.colorId,
                                    ootdId = clothPreview.ootd?.id
                                )
                            } ?: emptyList()


                            Log.d("FILTERED_ITEMS", "필터링된 아이템 개수: ${filteredItems.size}")

                            setupRecyclerView(filteredItems)
                        } ?: Log.e("API_ERROR", "Response Body is Null")
                    } else {
                        Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ClothesCategoryResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failure: ${t.message}")
                }
            })
    }

    private fun navigateToSearchFilterOotdFragment(selectedItem: Item_search) {
        val bundle = Bundle().apply {
            putInt("kindId", -1)
            selectedItem.ootdId?.let { putInt("ootd_id", it) }
            putString("imageUrl", selectedItem.imageUrl)
            putInt("categoryId", selectedItem.categoryId ?: -1)
            putInt("fitId", selectedItem.fitId ?: -1)
//            putIntegerArrayList("colorId", ArrayList(selectedItem.colorId?.let { listOf(it) } ?: emptyList()))
        }

        Log.d("SearchFilter", "전달하는 ootdId: ${selectedItem.ootdId}")
        findNavController().navigate(R.id.action_searchFilterFragment_to_bookmarkOotdFragment, bundle)
        Log.d("SearchFilter", "이동 성공인지 체크")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
