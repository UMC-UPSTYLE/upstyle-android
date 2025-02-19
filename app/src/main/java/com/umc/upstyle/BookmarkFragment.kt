package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.upstyle.data.model.BookmarkItem
import com.umc.upstyle.data.model.BookmarkResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentBookmarkBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecyclerAdapter_Bookmark
    // ✅ 변수 정의 필요
    private var bookmarkList: MutableList<BookmarkItem> = mutableListOf()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 버튼 ID 리스트
        val buttonIds = listOf(
            R.id.btn_all, R.id.btn_outer, R.id.btn_top, R.id.btn_bottom,
            R.id.btn_shoes, R.id.btn_bag, R.id.btn_other
        )

        // 기본 선택 버튼 (ALL)
        val selectedButton = binding.root.findViewById<ConstraintLayout>(R.id.btn_all)
        selectedButton.setBackgroundResource(R.drawable.bg_circle_yellow) // 기본 선택 배경 적용

        // 각 버튼 클릭 이벤트 설정
        buttonIds.forEach { id ->
            val button = binding.root.findViewById<ConstraintLayout>(id)
            button.setOnClickListener {
                selectButton(button)
                filterBookmarks(id)
            }
        }

        // RecyclerView 설정
        adapter = RecyclerAdapter_Bookmark(emptyList())
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        // 서버에서 북마크 리스트 불러오기
        loadBookmarksFromServer()



    //아이템 간격 추가 (아이템끼리 5dp, 화면 양끝 12.5dp)
        val spacingInPixels = (5 * resources.displayMetrics.density).toInt() // 5dp → px 변환
        val edgeSpacingInPixels = (12.5 * resources.displayMetrics.density).toInt() // 12.5dp → px 변환
        binding.recyclerView.addItemDecoration(LayoutSpace(2, spacingInPixels, edgeSpacingInPixels))

        // 초기 상태 불러오기
        filterBookmarks(R.id.btn_all)
    }
    private fun loadBookmarksFromServer() {
        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getBookmarks(null).enqueue(object : Callback<BookmarkResponse> {
            override fun onResponse(
                call: Call<BookmarkResponse>,
                response: Response<BookmarkResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    bookmarkList = (response.body()?.result?.bookmarkList ?: emptyList()).toMutableList()
                    Log.d("BookmarkFragment", "서버 데이터 불러오기 성공: $bookmarkList")  // 디버깅 로그 추가
                    filterBookmarks(R.id.btn_all)
                } else {
                    Log.e("BookmarkFragment", "서버 응답 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("BookmarkFragment", "네트워크 오류 발생: ${t.message}")
            }
        })
    }


    private fun selectButton(button: ConstraintLayout) {
        // 모든 버튼을 기본 색으로 변경
        val buttonIds = listOf(
            R.id.btn_all, R.id.btn_outer, R.id.btn_top, R.id.btn_bottom,
            R.id.btn_shoes, R.id.btn_bag, R.id.btn_other
        )

        buttonIds.forEach { id ->
            binding.root.findViewById<ConstraintLayout>(id)
                .setBackgroundResource(R.drawable.bg_circle_gray)
        }

        // 현재 선택된 버튼을 노란색으로 변경
        button.setBackgroundResource(R.drawable.bg_circle_yellow)
    }

    private fun filterBookmarks(selectedCategoryId: Int) {
        // 서버에서 가져온 bookmarkList를 기반으로 필터링
        val filteredItems: List<BookmarkItem> = when (selectedCategoryId) {
            R.id.btn_all -> bookmarkList
            R.id.btn_outer -> bookmarkList.filter { it.kind == "아우터" }
            R.id.btn_top -> bookmarkList.filter { it.kind == "상의" }
            R.id.btn_bottom -> bookmarkList.filter { it.kind == "하의" }
            R.id.btn_shoes -> bookmarkList.filter { it.kind == "신발" }
            R.id.btn_bag -> bookmarkList.filter { it.kind == "가방" }
            R.id.btn_other -> bookmarkList.filter { it.kind == "기타" }
            else -> bookmarkList
        }
        // RecyclerView 어댑터 업데이트
        adapter.updateList(filteredItems.map {
            val displayText = if (selectedCategoryId == R.id.btn_all) {
                ""  // ALL 버튼 클릭 시 텍스트 없음
            } else if (selectedCategoryId == R.id.btn_shoes || selectedCategoryId == R.id.btn_other) {
                "${it.category} ${it.color}"
            } else {
                "${it.category} ${it.fit} ${it.color}"  // 카테고리 핏 색깔 순서로 표시
            }

            Item_bookmark(
                name = displayText,
                image = it.ootd?.imageUrl ?: "",
                category = it.kind
            )
        })
    }



    private fun loadBookmarkState(key: String): Boolean {
        val preferences = requireActivity().getSharedPreferences("BookmarkPrefs", android.content.Context.MODE_PRIVATE)
        return preferences.getBoolean(key, false) // 기본값: false (북마크 해제 상태)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
