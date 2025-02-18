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
import androidx.activity.addCallback
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Safe Args로 전달된 데이터 수신
        val args = SearchItemFragmentArgs.fromBundle(requireArguments())
        category = args.category // Safe Args로 데이터 수신
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

        // 뒤로가기 버튼 클릭 이벤트
        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.searchFragment, false)
        }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.searchFragment, false)
        }

        val category = arguments?.getString("category") // 전달된 데이터 수신


        val bundle = Bundle().apply {
            putString("category", category)
        }

        // 컬러 필터링 filterButton
        binding.filterButton.setOnClickListener {
//            val action = SearchItemFragmentDirections
//                .actionSearchItemFragmentToClosetItemFilterFragment(category ?: "DEFAULT")
//            findNavController().navigate(action)
            findNavController().navigate(R.id.closetItemFilterFragment, bundle)
        }

        val args = SearchItemFragmentArgs.fromBundle(requireArguments())




        // 상단 제목 설정
        binding.titleText.text = "$category"


        // RecyclerView 설정
        val items = loadItemsFromPreferences()
        setupRecyclerView(items)

        val userId = 1
        val kindId = getkindId(category)

        // ✅ 서버에서 아이템 불러오기
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
        binding.recyclerView.adapter = RecyclerAdapter_Search(items) { selectedItem ->
            navigateToSearchResultFragment(selectedItem)
        }
    }


    private fun loadItemsFromPreferences(): List<Item_search> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        // 해당 카테고리에 맞는 텍스트와 이미지 경로 가져오기
        val description = preferences.getString(category, "$category 정보 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)

        // 기본 샘플 데이터
        val itemSearchs = mutableListOf(
            Item_search("샘플 1", "https://example.com/image1.jpg"),
            Item_search("샘플 2", "https://example.com/image2.jpg")
        )

        // 저장된 데이터 추가
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty() && description != "없음") {
            val file = File(savedImagePath)
            if (file.exists()) {
                val fileUri = Uri.fromFile(file)
                itemSearchs.add(0, Item_search(description, fileUri.toString()))
            }
        }

        return itemSearchs
    }


    private fun fetchItemsFromCloset(userId: Int, kindId: Int?) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        Log.d("CATEGORY_ID", "API 요청 - kindId: $kindId") // ✅ kindId 값 확인

        apiService.getClosetByCategory(userId, kindId).enqueue(object : retrofit2.Callback<ClosetCategoryResponse> {
            override fun onResponse(call: Call<ClosetCategoryResponse>, response: retrofit2.Response<ClosetCategoryResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { closetResponse ->
                        Log.d("API_RESPONSE", "서버 응답 개수: ${closetResponse.result.clothPreviewList.size}")

                        // ✅ 만약 서버에서 필터링이 안 되었다면, 클라이언트에서 수동 필터링
                        val filteredItems = closetResponse.result.clothPreviewList
                            .filter { item ->
                                kindId == null || item.kindId == kindId // 정확하게 kindId 일치 여부 체크
                            }

                            .map { clothPreview ->
                                val imageUrl = clothPreview.ootd?.imageUrl ?: "https://example.com/default_image.jpg"
                                val description = "${clothPreview.categoryName} ${clothPreview.fitName} ${clothPreview.colorName}"
                                Item_search(description, imageUrl)
                            }

                        Log.d("FILTERED_ITEMS", "필터링된 아이템 개수: ${filteredItems.size}") // ✅ 필터링된 결과 개수 확인

                        setupRecyclerView(filteredItems) // ✅ 필터링된 데이터 적용
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
                description = item.description // ✅ description 값 전달
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