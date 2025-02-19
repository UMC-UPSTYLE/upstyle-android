package com.umc.upstyle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.FragmentClosetResultBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.model.ClothPreview
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.util.ColorUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ClosetResultFragment : Fragment() {

    private var _binding: FragmentClosetResultBinding? = null
    private val binding get() = _binding!!
    private var categoryId: Int = 0
    private lateinit var selectedOptions: List<String>
    private lateinit var colorIds: List<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClosetResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // arguments에서 전달된 데이터 받기
        val where = arguments?.getString("category") ?: "기본값1"
        categoryId = arguments?.getInt("categoryId") ?: 0
        selectedOptions = ((arguments?.getIntegerArrayList("selectedOptions") ?: emptyList<String>()) as List<String>)


        // 색상 이름을 숫자로 변환하여 List<Int>로 만듦
        colorIds = selectedOptions.mapNotNull { colorName ->
            ColorUtil.getColorIdByName(colorName.toString())
        }

        // 어디서 온 건지 TextView에 데이터 설정 ex. OUTER, TOP
        binding.mainTitleTextView.text = where

        // arguments에서 전달된 리스트 받기
        val colorList = arguments?.getStringArrayList("selectedOptions") ?: arrayListOf()

        // 리스트를 ", "로 구분하여 하나의 문자열로 결합
        val resultText = colorList.joinToString(", ")

        // TextView에 선택된 컬러들 보여주기
        binding.tvSelectedOptions.text = resultText


        // 이전 Fragment로 이동
        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.closetItemFragment, false)
        }

        // 컬러 필터링 filterButton
        binding.filterButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("category", where)
            }
            findNavController().navigate(R.id.closetItemFilterFragment, bundle)
        }

        //API 호출
        fetchClosetItems()

    }

    private fun fetchClosetItems() {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getClosetByFilter(userId = 1, categoryId = categoryId, colorId = colorIds)
            .enqueue(object : Callback<ClosetCategoryResponse> {
                override fun onResponse(
                    call: Call<ClosetCategoryResponse>,
                    response: Response<ClosetCategoryResponse>
                ) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val items = response.body()?.result?.clothPreviewList ?: emptyList()
                        setupRecyclerView(items)
                        binding.tvResultCount.text = items.size.toString()
                    } else {
//                        binding.titleText.text = "데이터 불러오기 실패"
                    }
                }
                override fun onFailure(call: Call<ClosetCategoryResponse>, t: Throwable) {
                }
            })
    }
    // RecyclerView 설정
    private fun setupRecyclerView(items: List<ClothPreview>) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = RecyclerAdapter_Closet(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}