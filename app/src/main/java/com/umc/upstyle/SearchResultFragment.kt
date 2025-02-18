package com.umc.upstyle

import Item_result
import android.content.Context
import android.net.Uri
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
import java.io.File

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!! // Non-nullable로 사용하기 위한 접근자

    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arguments로 전달된 category 값 가져오기
        category = arguments?.getString("CATEGORY")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 초기화
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        val selectedDescription = arguments?.getString("description") // ✅ 전달된 description 받기
        val category = arguments?.getString("category") // ✅ category 가져오기


        // RecyclerView 설정
        val allItems = loadItemsFromPreferences()


        val bundle = Bundle().apply {
            putString("category", category)
        }

        // ✅ categoryId 가져오기 (필요 시 사용)
        val categoryId = arguments?.getInt("categoryId")


        // ✅ 선택한 description과 같은 아이템만 필터링
        val filteredItems = if (!selectedDescription.isNullOrEmpty()) {
            allItems.filter { it.description == selectedDescription }
        } else {
            allItems
        }

        setupRecyclerView(filteredItems) // ✅ 필터링된 아이템만 RecyclerView에 적용

        // ✅ API 데이터 가져오기 (categoryId가 있을 경우)
        if (categoryId != null) {
            fetchClothesByCategory(categoryId, selectedDescription)
        }
    }



    private fun setupRecyclerView(items: List<Item_result>) {

        // 검색 결과 개수를 UI에 반영
        binding.tvResultCount.text = items.size.toString()

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = RecyclerAdapter_Result(items) { selectedItem ->
            navigateToBookmarkOotdFragment(selectedItem)
        }
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }



    // 선택한 아이템을 전달하는 함수 추가
    private fun navigateToBookmarkOotdFragment(selectedItem: Item_result) {
        val bundle = Bundle().apply {
            putString("item_name", selectedItem.description)  // 아이템 이름
            putString("item_image", selectedItem.imageUrl)  // 이미지 URL
        }
        findNavController().navigate(R.id.action_searchResultFragment_to_bookmarkOotdtFragment, bundle)
    }




    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged() // 리사이클러뷰 어댑터 데이터 갱신
    }

    private fun loadItemsFromPreferences(): List<Item_result> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        val description = preferences.getString(category, "$category 정보 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)


        // 기본 샘플 데이터 -> ui확인용
        val itemResults = mutableListOf(
            Item_result("셔츠 슬림 블랙", "https://www.ocokorea.com//upload/images/product/148/148607/Product_1693647123947.jpg"),
            Item_result("코튼팬츠 오버핏 그레이", "https://sitem.ssgcdn.com/70/26/15/item/1000363152670_i1_750.jpg"),
            Item_result("무스탕 레귤러 오렌지", "https://m.likeygirl.kr/web/product/big/20231204_000027_LK.jpg")

        )



        // 저장된 데이터 추가
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty() && description != "없음") {
            itemResults.add(Item_result(description, savedImagePath))
        }

        return itemResults
    }

    private fun fetchClothesByCategory(categoryId: Int?, selectedDescription: String?) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getClothesByCategory(kindId = null, categoryId = categoryId, colorIds = null, fitId = null)
            .enqueue(object : retrofit2.Callback<ClothesCategoryResponse> {
                override fun onResponse(call: Call<ClothesCategoryResponse>, response: retrofit2.Response<ClothesCategoryResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { clothesResponse ->
                            Log.d("API_RESPONSE", "Received ${clothesResponse.result.clothPreviewList.size} items") // ✅ 응답 로그 확인

                            val allItems = clothesResponse.result.clothPreviewList.map { clothPreview ->
                                val imageUrl = clothPreview.ootd?.imageUrls?.firstOrNull() ?: "https://example.com/default_image.jpg"
                                val description = "${clothPreview.categoryName} ${clothPreview.fitName} ${clothPreview.colorName}"
                                Item_result(description, imageUrl)
                            }

                            // ✅ 선택한 description과 같은 아이템만 필터링
                            val filteredItems = if (!selectedDescription.isNullOrEmpty()) {
                                allItems.filter { it.description == selectedDescription }
                            } else {
                                allItems
                            }

                            activity?.runOnUiThread {
                                setupRecyclerView(filteredItems) // ✅ 필터링된 데이터만 RecyclerView에 적용
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ClothesCategoryResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }





    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지: 뷰가 파괴되면 바인딩 해제
        _binding = null
    }

    /*companion object {
        fun newInstance(category: String): ClosetItemFragment {
            val fragment = ClosetItemFragment()
            val args = Bundle()
            args.putString("CATEGORY", category) // 카테고리 전달
            fragment.arguments = args
            return fragment
        }
    }*/

}