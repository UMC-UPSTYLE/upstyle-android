package com.umc.upstyle

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.model.ClothPreview
import com.umc.upstyle.data.model.Ootd
import com.umc.upstyle.data.model.User
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentLoadItemBinding
import com.umc.upstyle.utils.Item_load
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LoadItemFragment : Fragment() {

    private var _binding: FragmentLoadItemBinding? = null
    private val binding get() = _binding!!

    private var category: String? = null
    private var categoryId: Long? = null
    private var selectedItem: Item_load? = null
    private lateinit var adapter: RecyclerAdapter_Load

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = LoadItemFragmentArgs.fromBundle(requireArguments())
        category = args.category

        // category 값을 기반으로 categoryId 매핑
        categoryId = when (category) {
            "OUTER" -> 1L
            "TOP" -> 2L
            "BOTTOM" -> 3L
            "SHOES" -> 4L
            "BAG" -> 5L
            "OTHER" -> 6L
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 초기에는 카테고리 이름만 표시
        binding.itemText.text = category ?: "OTHER"

        adapter = RecyclerAdapter_Load(mutableListOf()) { item, position ->
            selectedItem = item
            adapter.setSelectedPosition(position)

            // 선택한 아이템의 설명 표시
            binding.itemText.text = "${category ?: "OTHER"}: ${item.description}"
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        fetchClosetItems()

        binding.btSelect.setOnClickListener {
            if (selectedItem != null) {
                val clothPreviewItem = ClothPreview(
                    id = 1,
                    kindId = 0L,
                    kindName = selectedItem!!.description,
                    categoryId = categoryId ?: 0L,
                    categoryName = category ?: "OTHER",
                    fitId = 0L,
                    fitName = "Default Fit",
                    colorId = 0L,
                    colorName = "Default Color",
                    additionalInfo = null,
                    ootd = Ootd(
                        id = -1L,
                        user = User(id = 1, nickname = "Default User"),
                        date = "2024-01-01",
                        imageUrl = selectedItem!!.imageUrl,
                        clothList = emptyList()
                    )
                )
                sendSelectedItemToPreviousFragment(clothPreviewItem)
            } else {
                Toast.makeText(requireContext(), "아이템을 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun fetchClosetItems() {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getClosetByCategory(userId = 1L, categoryId = categoryId)
            .enqueue(object : Callback<ClosetCategoryResponse> {
                override fun onResponse(
                    call: Call<ClosetCategoryResponse>,
                    response: Response<ClosetCategoryResponse>
                ) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val serverItems = response.body()?.result?.clothPreviewList ?: emptyList()

                        // ClothPreview → Item_load로 변환
                        val serverItemLoads = serverItems.map {
                            Item_load(
                                description = it.kindName,
                                imageUrl = it.ootd?.imageUrl ?: ""
                            )
                        }

                        val localItems = loadItemsFromPreferences()

                        // 서버 데이터와 로컬 데이터를 합쳐서 MutableList로 변환
                        val combinedItems = (serverItemLoads + localItems).toMutableList()

                        //MutableList<Item_load>로 어댑터에 전달
                        adapter.updateData(combinedItems)
                    } else {
                        Toast.makeText(requireContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ClosetCategoryResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun sendSelectedItemToPreviousFragment(selectedItem: ClothPreview) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM", selectedItem.kindName)
        findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM_IMAGE_URL", selectedItem.ootd?.imageUrl)
        findNavController().previousBackStackEntry?.savedStateHandle?.set("CATEGORY", category)
        findNavController().popBackStack()
    }









    private fun loadItemsFromPreferences(): List<Item_load> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        // 해당 카테고리에 맞는 텍스트와 이미지 경로 가져오기
        val description = preferences.getString(category, "$category 정보 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)

        // 기본 샘플 데이터
        val itemLoads = mutableListOf(
            Item_load("케로로", "https://image.cine21.com/resize/cine21/still/2005/1121/M0020066_focus52804[W578-].jpg"),
            Item_load("샘플 2", "https://example.com/image2.jpg"),
            Item_load("샘플 1", "https://example.com/image1.jpg"),
            Item_load("샘플 2", "https://example.com/image2.jpg"),
            Item_load("샘플 1", "https://example.com/image1.jpg"),
            Item_load("샘플 2", "https://example.com/image2.jpg"),
            Item_load("샘플 1", "https://example.com/image1.jpg"),
            Item_load("샘플 2", "https://example.com/image2.jpg"),
            Item_load("샘플 1", "https://example.com/image1.jpg"),
            Item_load("샘플 2", "https://example.com/image2.jpg")
        )

        // 저장된 데이터 추가 + 없으면 걍 안뜨게
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty() && description != "없음") {
            val file = File(savedImagePath)
            if (file.exists()) {
                val fileUri = Uri.fromFile(file)
                itemLoads.add(0, Item_load(description, fileUri.toString()))
            }
        }

        return itemLoads
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 뷰 바인딩 해제
    }

    /* companion object {
         fun newInstance(category: String): LoadItemFragment {
             val fragment = LoadItemFragment()
             val args = Bundle()
             args.putString("CATEGORY", category) // 카테고리 전달
             fragment.arguments = args
             return fragment
         }
     }*/
}