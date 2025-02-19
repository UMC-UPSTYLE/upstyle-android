package com.umc.upstyle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.BookmarkRequest
import com.umc.upstyle.data.model.BookmarkResponse
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.model.Ootd
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.OotdApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.databinding.FragmentSearchFilterOotdBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFilterOotdFragment : Fragment(R.layout.fragment_search_filter_ootd) {

    // ViewBinding 객체 선언
    private var _binding: FragmentSearchFilterOotdBinding? = null
    private val binding get() = _binding!!
    private val apiService = RetrofitClient.createService(OotdApiService::class.java)
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)

    private var description: String? = null
    private var imageUrl: String? = null
    private var categoryId: Int = -1
    private var fitId: Int = -1
    private var colorId: Int = -1

    private var ootdId: Int = -1
    private var clothId: Int = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFilterOotdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).binding.bottomNavigationView.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).binding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        val kindId = arguments?.getInt("kindId") ?: -1
        val imageUrl = arguments?.getInt("imageUrl") ?: -1
        val categoryId = arguments?.getInt("categoryId") ?: -1
        val fitId = arguments?.getInt("fitId") ?: -1
        val colorId = arguments?.getInt("colorId") ?: -1
        val clothId = arguments?.getInt("cloth_id") ?: -1

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        val categoryName = arguments?.getString("category_name") ?: "Unknown"
        val fitName = arguments?.getString("fit_name") ?: "Unknown"
        val colorName = arguments?.getString("color_name") ?: "Unknown"

        binding.outerText.setTextColor(resources.getColor(android.R.color.white, null))
        binding.bottomText.setTextColor(resources.getColor(android.R.color.white, null))
        binding.topText.setTextColor(resources.getColor(android.R.color.white, null))
        binding.shoesText.setTextColor(resources.getColor(android.R.color.white, null))
        binding.bagText.setTextColor(resources.getColor(android.R.color.white, null))
        binding.otherText.setTextColor(resources.getColor(android.R.color.white, null))

        // ✅ kindId에 맞게 적절한 TextView에 텍스트 설정
        val itemDescription = "$categoryName $fitName $colorName"

        fetchOotdData(ootdId)

        // ✅ 북마크 상태 관리
        setupBookmarkButtons(clothId)
    }

    // ✅ 북마크 버튼 설정 함수
    private fun setupBookmarkButtons(kindId: Int?) {
        if (kindId == null) return

        val bookmarkButtons = listOf(
            binding.bookmarkOuter to "bookmark_outer_$kindId",
            binding.bookmarkTop to "bookmark_top_$kindId",
            binding.bookmarkBottom to "bookmark_bottom_$kindId",
            binding.bookmarkShoes to "bookmark_shoes_$kindId",
            binding.bookmarkBag to "bookmark_bag_$kindId",
            binding.bookmarkOther to "bookmark_other_$kindId"
        )

        val bookmarkStates = mutableMapOf<String, Boolean>()
        bookmarkButtons.forEach { (button, key) ->
            val isBookmarked = loadBookmarkState(key)
            bookmarkStates[key] = isBookmarked
            button.setImageResource(if (isBookmarked) R.drawable.bookmark_on else R.drawable.bookmark_off)

            button.setOnClickListener {
                val newState = !bookmarkStates[key]!!
                bookmarkStates[key] = newState
                button.setImageResource(if (newState) R.drawable.bookmark_on else R.drawable.bookmark_off)

                toggleBookmark(kindId, newState)
            }
        }
    }

    // ✅ 북마크 저장/취소 API 호출
    private fun toggleBookmark(kindId: Int, isBookmarked: Boolean) {
        val userId = 1 // 예시: 현재 로그인한 사용자 ID
        val request = BookmarkRequest(userId, kindId) // kindId를 clothId로 전달

        val bookmarkApi = RetrofitClient.createService(ApiService::class.java)

        bookmarkApi.toggleBookmark(request).enqueue(object : Callback<BookmarkResponse> {
            override fun onResponse(
                call: Call<BookmarkResponse>,
                response: Response<BookmarkResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("Bookmark", "북마크 변경 완료: $isBookmarked")
                } else {
                    Log.e("Bookmark", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("Bookmark", "Failure: ${t.message}")
            }
        })
    }

    // 서버에서 데이터 가져오기
    private fun fetchOotdData(ootdId: Int) {
        apiService.getOOTDById(ootdId).enqueue(object : Callback<ApiResponse<Ootd>> {
            override fun onResponse(
                call: Call<ApiResponse<Ootd>>,
                response: Response<ApiResponse<Ootd>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.result?.let { ootd ->
                        Log.d("API_RESPONSE", "Fetched OOTD: $ootd")

                        Glide.with(requireContext())
                            .load(ootd.imageUrls?.getOrNull(0))
                            .into(binding.ootdImage)

                        Log.d("API_RESPONSE", "가져온 Image Url: ${ootd.imageUrls?.getOrNull(0)}")

                        binding.userName.text = ootd.user.nickname

                        ootd.clothList?.forEach { cloth ->
                            val categoryName = cloth.categoryName
                            val fitName = cloth.fitName
                            val colorName = cloth.colorName

                            // kindId에 따라 텍스트를 설정
                            when (cloth.kindId) {
                                1 -> binding.outerText.text = "$categoryName $fitName $colorName"
                                2 -> binding.topText.text = "$categoryName $fitName $colorName"
                                3 -> binding.bottomText.text = "$categoryName $fitName $colorName"
                                4 -> binding.shoesText.text = "$categoryName $colorName"
                                5 -> binding.bagText.text = "$categoryName $fitName $colorName"
                                6 -> binding.otherText.text = "$categoryName $colorName"
                                else -> Log.d(
                                    "BookmarkOotdFragment",
                                    "Unknown kindId: ${cloth.kindId}"
                                )
                            }
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Failed to fetch data for OOTD ID: $ootdId")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Ootd>>, t: Throwable) {
                Log.e("API_ERROR", "Request failed", t)
            }
        })
    }

    // ✅ 북마크 상태 저장/로드
    private fun saveBookmarkState(key: String, isBookmarked: Boolean) {
        val preferences = requireActivity().getSharedPreferences(
            "BookmarkPrefs",
            android.content.Context.MODE_PRIVATE
        )
        preferences.edit().putBoolean(key, isBookmarked).apply()
    }

    private fun loadBookmarkState(key: String): Boolean {
        val preferences = requireActivity().getSharedPreferences(
            "BookmarkPrefs",
            android.content.Context.MODE_PRIVATE
        )
        return preferences.getBoolean(key, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}