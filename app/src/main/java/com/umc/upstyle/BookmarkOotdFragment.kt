package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.umc.upstyle.data.Item
import com.umc.upstyle.data.model.BookmarkRequest
import com.umc.upstyle.data.model.BookmarkResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentBookmarkOotdBinding
import retrofit2.Call

// 이미지, 옷 정보, 사용자 이름 받아서
// 1. 이미지 추가
// 2. 옷 정보 추가
// 3. 사용자 이름 추가

class BookmarkOotdFragment : Fragment() {

    // ViewBinding 객체 선언
    private var _binding: FragmentBookmarkOotdBinding? = null
    private val binding get() = _binding!!

    private var userName: String? = null // 사용자 이름
    private var selectedCategory: String? = null // OUTER, TOP, BOTTOM 등
    private var selectedSubCategory: String? = null // selectedCategory의 카테고리
    private var selectedFit: String? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentBookmarkOotdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // View Binding을 통해 BottomNavigationView 숨기기
        val mainBinding = (activity as MainActivity).binding // MainActivity의 View Binding 객체 참조
        mainBinding.bottomNavigationView.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        // View Binding을 통해 BottomNavigationView 다시 보이기
        val mainBinding = (activity as MainActivity).binding // MainActivity의 View Binding 객체 참조
        mainBinding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        // 이전 Fragment로 이동
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // XML에 정의된 ootdImage를 찾기
        val ootdImageView: ImageView = view.findViewById(R.id.ootdImage)

        // 데이터 수신
        val userId = 1  //현재 로그인한 사용자 ID (임시 값)

        val itemName = arguments?.getString("item_name") ?: "아이템 이름 없음"
        val ootdImage = arguments?.getString("item_image") ?: ""
        val clothId = arguments?.getInt("cloth_id") ?: 0 // 받아온 clothId

        //아이템 구분 위해 username을 넣긴 했지만 나중에는 번호 부여 해야할듯..
        val userName = arguments?.getString("user_name") ?: "사용자"


        // 이미지가 존재하면 Glide를 사용해 ootdImageView에 로드
        if (ootdImage.isNotEmpty()) {
            Glide.with(this)
                .load(ootdImage)
                .into(ootdImageView)
        }

        // 사용자 이름 받기
        binding.userName.text = "$userName"


        // OUTER 항목 옆에 이름 추가 / 일단 추가
        binding.outerText.text = itemName


        // 북마크
        var isBookmarked = false

        val bookmarkButtons = listOf(
            binding.bookmarkOuter to "bookmark_outer_$clothId",
            binding.bookmarkTop to "bookmark_top_$clothId",
            binding.bookmarkBottom to "bookmark_bottom_$clothId",
            binding.bookmarkBag to "bookmark_bag_$clothId",
            binding.bookmarkShoes to "bookmark_shoes_$clothId",
            binding.bookmarkOther to "bookmark_other_$clothId"

        )


        // 초기 상태 불러오기
        val bookmarkStates = mutableMapOf<String, Boolean>()
        bookmarkButtons.forEach { (button, key) ->
            val isBookmarked = loadBookmarkState(key)
            bookmarkStates[key] = isBookmarked
            button.setImageResource(if (isBookmarked) R.drawable.bookmark_on else R.drawable.bookmark_off)

            button.setOnClickListener {
                val newState = !bookmarkStates[key]!!
                bookmarkStates[key] = newState
                button.setImageResource(if (newState) R.drawable.bookmark_on else R.drawable.bookmark_off)

                // API 호출 추가 (Retrofit 사용)
                //toggleBookmark(userId, clothId, newState)


            }
            // 데이터 리스트를 기반으로 동적으로 뷰 생성
            /*for (item in items) {
                val rowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 16.dpToPx() // 각 행의 간격
                    }
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 0, 0) // 패딩을 없앰
                }

                val iconView = ImageView(requireContext()).apply {
                    setImageResource(item.iconRes)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = 0 // 왼쪽에 딱 붙도록 간격 제거
                        marginEnd = 16.dpToPx() // 아이콘과 텍스트 간 간격
                    }
                }

                val textView = TextView(requireContext()).apply {
                    text = buildString {
                        append(item.subCategory) // subCategory 추가
                        item.fit?.let { append(" $it") } // fit 추가 (null이 아니면 공백 후 추가)
                        item.size?.let { append(" $it") } // size 추가 (null이 아니면 공백 후 추가)
                        append(" ${item.color}") // color 항상 추가
                    }
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // 아이템을 부모 레이아웃에 추가
                rowLayout.addView(iconView)
                rowLayout.addView(textView)
                //binding.parentLayout.addView(rowLayout) // ViewBinding 사용
            }
        }
    }

    // 북마크 저장/취소 API 호출
    private fun toggleBookmark(userId: Int, clothId: Int, button: ImageView) {
        val request = BookmarkRequest(userId, clothId)

        val bookmarkApi = RetrofitClient.createService(ApiService::class.java)

        bookmarkApi.toggleBookmark(request).enqueue(object : retrofit2.Callback<BookmarkResponse> {
            override fun onResponse(call: Call<BookmarkResponse>, response: retrofit2.Response<BookmarkResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.result
                    result?.let {
                        updateBookmarkUI(button, it.isBookmarked) // ✅ 버튼 이미지 변경
                    }
                } else {
                    Log.e("Bookmark", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("Bookmark", "Failure: ${t.message}")
            }
        })
    }

    private fun updateBookmarkUI(button: ImageView, isBookmarked: Boolean) {
        button.setImageResource(if (isBookmarked) R.drawable.bookmark_on else R.drawable.bookmark_off)
    }*/
        }
    }



    private fun saveBookmarkState(key: String, isBookmarked: Boolean, userId: String, clothId: String) {
        val preferences = requireActivity().getSharedPreferences("BookmarkPrefs", android.content.Context.MODE_PRIVATE)
        val editor = preferences.edit()

        if (isBookmarked) {
            editor.putBoolean(key, true)
            editor.putString("${key}_user", userId)
            editor.putString("${key}_cloth", clothId)
        } else {
            editor.remove(key)
            editor.remove("${key}_user")
            editor.remove("${key}_cloth")
        }
        editor.apply()
    }

    private fun loadBookmarkState(key: String): Boolean {
        val preferences = requireActivity().getSharedPreferences("BookmarkPrefs", android.content.Context.MODE_PRIVATE)
        return preferences.getBoolean(key, false)
    }



    // dp 값을 px로 변환하는 함수
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 ViewBinding 해제
    }
}