package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.BookmarkRequest
import com.umc.upstyle.data.model.BookmarkResponse
import com.umc.upstyle.data.model.Cloth
import com.umc.upstyle.data.model.Ootd
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.OotdApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentBookmarkOotdBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookmarkOotdFragment : Fragment() {

    // ViewBinding 객체 선언
    private var _binding: FragmentBookmarkOotdBinding? = null
    private val binding get() = _binding!!
    private val apiService = RetrofitClient.createService(OotdApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkOotdBinding.inflate(inflater, container, false)
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

        // ✅ arguments에서 데이터 직접 가져오기 (Safe Args 사용 X)

        val clothId = arguments?.getInt("cloth_id") ?: -1
        val ootdId = arguments?.getInt("ootd_id") ?: -1
//        val clothList: ArrayList<Cloth>? = arguments?.getParcelableArrayList("clothList")
        Log.d("BookmarkOotdFragment", "받아온 cloth_id: $clothId")
        Log.d("BookmarkOotdFragment", "받아온 ootd_id: $ootdId")
        val categoryId = arguments?.getInt("category_id") ?: -1
        val fitId = arguments?.getInt("fit_id") ?: -1
        val colorId = arguments?.getInt("color_id") ?: -1

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        val categoryName = arguments?.getString("category_name") ?: "Unknown"
        val fitName = arguments?.getString("fit_name") ?: "Unknown"
        val colorName = arguments?.getString("color_name") ?: "Unknown"

        // ✅ 사용자 이름 표시
        binding.userName.text = " "

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
    private fun setupBookmarkButtons(clothId: Int) {
        if (clothId == -1) return

        val bookmarkButtons = listOf(
            binding.bookmarkOuter,
            binding.bookmarkTop,
            binding.bookmarkBottom,
            binding.bookmarkShoes,
            binding.bookmarkBag,
            binding.bookmarkOther
        )

        val jwtToken = requireActivity()
            .getSharedPreferences("Auth", android.content.Context.MODE_PRIVATE)
            .getString("jwt_token", null)

        if (jwtToken.isNullOrEmpty()) {
            Log.e("Bookmark", "❌ JWT 토큰이 없음")
            return
        }

        val bookmarkApi = RetrofitClient.createService(ApiService::class.java)

        bookmarkApi.getBookmarkStatus("Bearer $jwtToken", clothId).enqueue(object : Callback<BookmarkResponse> {
            override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val isBookmarked = response.body()!!.result?.isBookmarked ?: false

                    bookmarkButtons.forEach { button ->
                        button.setImageResource(if (isBookmarked) R.drawable.bookmark_on else R.drawable.bookmark_off)

                        button.setOnClickListener {
                            toggleBookmark(clothId, button)
                        }
                    }
                } else {
                    Log.e("Bookmark", "❌ 북마크 상태 가져오기 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("Bookmark", "❌ 서버 요청 실패: ${t.message}")
            }
        })
    }



    // ✅ 북마크 저장/취소 API 호출
    private fun toggleBookmark(clothId: Int, button: ImageView) {
        val jwtToken = requireActivity()
            .getSharedPreferences("Auth", android.content.Context.MODE_PRIVATE)
            .getString("jwt_token", null)

        if (jwtToken.isNullOrEmpty()) {
            Log.e("Bookmark", "❌ JWT 토큰이 없음")
            return
        }

        val bookmarkApi = RetrofitClient.createService(ApiService::class.java)

        val request = BookmarkRequest(clothId,clothId)

        bookmarkApi.toggleBookmark("Bearer $jwtToken", request).enqueue(object : Callback<BookmarkResponse> {
            override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarkResult = response.body()!!.result
                    val newBookmarkState = bookmarkResult?.isBookmarked ?: false

                    Log.d("Bookmark", "✅ 북마크 변경 완료: $newBookmarkState")

                    button.setImageResource(if (newBookmarkState) R.drawable.bookmark_on else R.drawable.bookmark_off)
                } else {
                    Log.e("Bookmark", "❌ 북마크 변경 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("Bookmark", "❌ 서버 요청 실패: ${t.message}")
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