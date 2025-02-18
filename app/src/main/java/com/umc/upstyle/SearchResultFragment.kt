package com.umc.upstyle

import Item_result
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.data.model.ClothesCategoryResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentSearchResultBinding
import retrofit2.Call

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!! // Non-nullable로 사용하기 위한 접근자

    private var categoryId: Int? = null
    private var fitId: Int? = null
    private var colorId: Int? = null
    private var selectedDescription: String? = null // ✅ 선택한 아이템 설명 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Safe Args를 통해 전달된 데이터 받기
        val args = SearchResultFragmentArgs.fromBundle(requireArguments())
        categoryId = args.categoryId
        fitId = args.fitId
        colorId = args.colorId
        selectedDescription = args.description

        Log.d("SearchResultFragment", "Received categoryId: $categoryId, fitId: $fitId, colorId: $colorId, description: $selectedDescription")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Log.d("SearchResultFragment", "Fetching data for categoryId: $categoryId, fitId: $fitId, colorId: $colorId")

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        val selectedDescription = arguments?.getString("description") // ✅ 전달된 description 받기
        val category = arguments?.getString("category") // ✅ category 가져오기

        

        val bundle = Bundle().apply {
            putString("category", category)
        }

        // ✅ categoryId 가져오기 (필요 시 사용)
        val categoryId = arguments?.getInt("categoryId")


        // ✅ API 데이터 가져오기
        if (categoryId != null) {
            fetchClothesByCategory(categoryId, fitId, colorId)
        }
    }

    private fun setupRecyclerView(items: List<Item_result>) {
        // 검색 결과 개수를 UI에 반영
        binding.tvResultCount.text = items.size.toString()

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = RecyclerAdapter_Result(items) { selectedItem ->
            Log.d("RecyclerView", "Clicked item: $selectedItem")
            navigateToBookmarkOotdFragment(selectedItem)
        }
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    // ✅ 같은 속성을 가진 다른 사람의 아이템 불러오기
    private fun fetchClothesByCategory(categoryId: Int?, fitId: Int?, colorId: Int?) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        Log.d("API_REQUEST", "Fetching clothes with categoryId: $categoryId, fitId: $fitId, colorId: $colorId")

        apiService.getClothesByCategory(
            kindId = null,  //kindId 필터를 아예 없앰
            categoryId = categoryId,
            colorIds = listOfNotNull(colorId),
            fitId = fitId
        )

            .enqueue(object : retrofit2.Callback<ClothesCategoryResponse> {
                override fun onResponse(call: Call<ClothesCategoryResponse>, response: retrofit2.Response<ClothesCategoryResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { clothesResponse ->
                            Log.d("API_RESPONSE", "Received ${clothesResponse.result.clothPreviewList.size} items")

                            val filteredItems = clothesResponse.result.clothPreviewList.map { clothPreview ->
                                val imageUrl = clothPreview.ootd?.imageUrls?.firstOrNull() ?: "https://example.com/default_image.jpg"
                                val description = listOfNotNull(clothPreview.categoryName, clothPreview.fitName, clothPreview.colorName)
                                    .joinToString(" ")

                                // ✅ 모든 데이터를 포함한 Item_result 생성
                                Item_result(
                                    description = description,
                                    imageUrl = imageUrl,
                                    kindId = clothPreview.kindId,
                                    kindName = clothPreview.kindName,
                                    categoryId = clothPreview.categoryId,
                                    categoryName = clothPreview.categoryName,
                                    fitId = clothPreview.fitId,
                                    fitName = clothPreview.fitName,
                                    colorId = clothPreview.colorId,
                                    colorName = clothPreview.colorName
                                )
                            }

                            activity?.runOnUiThread {
                                if (filteredItems.isNotEmpty()) {
                                    setupRecyclerView(filteredItems)
                                } else {
                                    Log.e("SearchResultFragment", "filteredItems 리스트가 비어 있음!")
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ClothesCategoryResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }



    // ✅ 선택한 아이템을 OOTD 북마크로 전달하는 함수
    private fun navigateToBookmarkOotdFragment(selectedItem: Item_result) {
        val bundle = Bundle().apply {
            putInt("kind_id", -1)  // ✅ 모든 kindId 포함을 위해 -1 또는 null 사용
            putInt("category_id", selectedItem.categoryId ?: -1)
            putInt("fit_id", selectedItem.fitId ?: -1)
            putInt("color_id", selectedItem.colorId ?: -1)

            putString("category_name", selectedItem.categoryName ?: "Unknown")
            putString("fit_name", selectedItem.fitName ?: "Unknown")
            putString("color_name", selectedItem.colorName ?: "Unknown")
        }

        Log.d("Navigation", "Navigating to BookmarkOotdFragment with bundle: $bundle")

        try {
            findNavController().navigate(R.id.action_searchResultFragment_to_bookmarkOotdFragment, bundle)
        } catch (e: Exception) {
            Log.e("Navigation Error", "Navigation failed: ${e.message}")
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ✅ 메모리 누수 방지: 뷰 바인딩 해제
    }
}
