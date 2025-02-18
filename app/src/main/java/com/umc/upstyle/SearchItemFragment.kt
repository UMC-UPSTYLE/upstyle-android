package com.umc.upstyle

import Item_search
import RecyclerAdapter_Search
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentSearchItemBinding
import retrofit2.Call
import java.io.File

class SearchItemFragment : Fragment() {

    private var _binding: FragmentSearchItemBinding? = null
    private val binding get() = _binding!!

    private var category: String? = null
    private var kindId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = SearchItemFragmentArgs.fromBundle(requireArguments())
        category = args.category // Safe Args로 받은 category
        kindId = args.kindId // Safe Args로 받은 kindId

        Log.d("SearchItemFragment", "Received category: $category, kindId: $kindId")
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.titleText.text = category ?: "카테고리 없음"

        val userId = 1

        Log.d("SearchItemFragment", "Fetching items for kindId: $kindId")

        // ✅ Safe Args로 받은 kindId를 그대로 사용
        fetchItemsFromCloset(userId, kindId)
    }


    private fun getkindId(category: String?): Int? {
        return when (category) {
            "아우터" -> 1
            "상의" -> 2
            "하의" -> 3
            "신발" -> 4
            "가방" -> 5
            "아더" -> 6
            else -> null // 기본값 설정 (null이면 전체 조회 가능하도록)
        }
    }



    private fun setupRecyclerView(items: List<Item_search>) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        if (items.isEmpty()) {
            Log.w("RecyclerView", "No items to display!") // ✅ 로그 추가
        }

        binding.recyclerView.adapter = RecyclerAdapter_Search(items) { selectedItem ->
            navigateToSearchResultFragment(selectedItem)
        }
    }

    private fun fetchItemsFromCloset(userId: Int, kindId: Int?) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        Log.d("API_REQUEST", "Fetching items for kindId: $kindId")

        apiService.getClosetByCategory(userId, kindId).enqueue(object : retrofit2.Callback<ClosetCategoryResponse> {
            override fun onResponse(call: Call<ClosetCategoryResponse>, response: retrofit2.Response<ClosetCategoryResponse>) {
                Log.d("API_RESPONSE", "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { closetResponse ->
                        Log.d("API_RESPONSE", "Closet Response: ${closetResponse.result.clothPreviewList}")

                        val filteredItems = closetResponse.result.clothPreviewList.mapNotNull { clothPreview ->
                            // null 또는 "null" 문자열 제거
                            val categoryName = clothPreview.categoryName?.takeIf { it.lowercase() != "null" }
                            val fitName = clothPreview.fitName?.takeIf { it.lowercase() != "null" }
                            val colorName = clothPreview.colorName?.takeIf { it.lowercase() != "null" }

                            // 유효한 값만 리스트에 추가
                            val description = listOfNotNull(categoryName, fitName, colorName).joinToString(" ")

                            if (categoryName == null) {
                                Log.w("FILTER", "Skipping item with null categoryName")
                                return@mapNotNull null
                            }

                            val imageUrl = clothPreview.ootd?.imageUrl ?: "https://example.com/default_image.jpg"

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




    private fun navigateToSearchResultFragment(item: Item_search) {
        val action = SearchItemFragmentDirections
            .actionSearchItemFragmentToSearchResultFragment(
                imageUrl = item.imageUrl,
                description = item.description,      // ✅ 여기에 description 추가!
                categoryId = item.categoryId ?: -1,
                fitId = item.fitId ?: -1,
                colorId = item.colorId ?: -1
            )
        findNavController().navigate(action)
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 뷰 바인딩 해제
    }

    companion object {
        fun newInstance(category: String): SearchItemFragment {
            val fragment = SearchItemFragment()
            val args = Bundle()
            args.putString("CATEGORY", category)
            fragment.arguments = args
            return fragment
        }
    }
}