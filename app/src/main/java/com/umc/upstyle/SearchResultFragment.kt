package com.umc.upstyle

import Item_result
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.databinding.FragmentSearchResultBinding
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


        val category = arguments?.getString("category") // 전달된 데이터 수신


        val bundle = Bundle().apply {
            putString("category", category)
        }




        // RecyclerView 설정
        val items = loadItemsFromPreferences()
        setupRecyclerView(items)
    }

    private fun setupRecyclerView(items: List<Item_result>) {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = RecyclerAdapter_Result(items) {
            navigateToBookmarkOotdFragment()
        }

    }


    private fun navigateToBookmarkOotdFragment() {
        findNavController().navigate(R.id.action_searchResultFragment_to_bookmarkOotdtFragment)
    }



    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged() // 리사이클러뷰 어댑터 데이터 갱신
    }

    private fun loadItemsFromPreferences(): List<Item_result> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        // 해당 카테고리에 맞는 텍스트와 이미지 경로 가져오기
        val description = preferences.getString(category, "$category 정보 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)

        // 기본 샘플 데이터 -> ui확인용
        val itemResults = mutableListOf(
            Item_result("루스한 오버핏 블랙", "https://www.ocokorea.com//upload/images/product/148/148607/Product_1693647123947.jpg"),
            Item_result("오버핏 레귤러 블랙", "https://sitem.ssgcdn.com/70/26/15/item/1000363152670_i1_750.jpg"),
            Item_result("일단 아무 텍스트", "https://m.likeygirl.kr/web/product/big/20231204_000027_LK.jpg")

        )

        // 저장된 데이터 추가
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty() && description != "없음") {
            val file = File(savedImagePath)
            if (file.exists()) {
                val fileUri = Uri.fromFile(file)
                itemResults.add(0, Item_result(description, fileUri.toString()))
            }
        }

        return itemResults
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