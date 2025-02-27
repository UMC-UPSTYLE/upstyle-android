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

    // OOTD 데이터를 저장할 변수
    private var ootdData: Ootd? = null

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

        // ✅ 북마크 버튼 리스트를 만들어서 클릭 리스너 설정
        val bookmarkButtons = listOf(
            binding.bookmarkOuter to 1,
            binding.bookmarkTop to 2,
            binding.bookmarkBottom to 3,
            binding.bookmarkShoes to 4,
            binding.bookmarkBag to 5,
            binding.bookmarkOther to 6
        )

        for ((button, kindId) in bookmarkButtons) {
            button.setOnClickListener {
                toggleBookmarkForKindId(kindId)
            }
        }
    }

    // ✅ 해당 kindId에 맞는 clothId를 찾아 toggleBookmark 호출
    private fun toggleBookmarkForKindId(kindId: Int) {
        val ootd = getOotdData()  // OOTD 데이터를 가져오는 함수 (이미 fetchOotdData()에서 데이터를 가져왔을 때 저장된 데이터 사용)

        // kindId에 해당하는 Cloth 객체를 찾는다
        val cloth = ootd?.clothList?.find { it.kindId == kindId }

        if (cloth != null) {
            // 해당 Cloth의 clothId를 이용해 북마크 토글
            toggleBookmark(cloth.id)
        } else {
            Log.d("BookmarkOotdFragment", "❌ 해당 kindId($kindId)의 아이템을 찾을 수 없습니다.")
        }
    }

    // ✅ 북마크 저장/취소 API 호출
    private fun toggleBookmark(clothId: Int) {
        val bookmarkApi = RetrofitClient.createService(ApiService::class.java)
        val request = BookmarkRequest(clothId) // JSON 형식으로 변환

        bookmarkApi.toggleBookmark(request).enqueue(object : Callback<BookmarkResponse> {
            override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bookmarkResult = response.body()!!.result
                    val newBookmarkState = bookmarkResult?.isBookmarked ?: false

                    Log.d("Bookmark", "✅ 북마크 변경 완료: $newBookmarkState")

                    // 클릭한 버튼을 업데이트
                    updateBookmarkButtonState(clothId, newBookmarkState)
                } else {
                    Log.e("Bookmark", "❌ 호출한 clothId: ${clothId}")
                    Log.e("Bookmark", "❌ 북마크 변경 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                Log.e("Bookmark", "❌ 서버 요청 실패: ${t.message}")
            }
        })
    }

    // ✅ 북마크 버튼 상태 업데이트 함수
    private fun updateBookmarkButtonState(clothId: Int, isBookmarked: Boolean) {
        // 북마크 상태에 맞게 버튼 이미지를 업데이트
        val bookmarkButtons = listOf(
            binding.bookmarkOuter,
            binding.bookmarkTop,
            binding.bookmarkBottom,
            binding.bookmarkShoes,
            binding.bookmarkBag,
            binding.bookmarkOther
        )

        bookmarkButtons.forEach { button ->
            val tagValue = button.tag
            if (tagValue is Int && tagValue == clothId) {
                button.setImageResource(if (isBookmarked) R.drawable.bookmark_on else R.drawable.bookmark_off)
            }
        }
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
                        ootdData = ootd  // 데이터를 클래스 내부 변수에 저장

                        Glide.with(requireContext())
                            .load(ootd.imageUrls?.getOrNull(0))
                            .into(binding.ootdImage)

                        Log.d("API_RESPONSE", "가져온 Image Url: ${ootd.imageUrls?.getOrNull(0)}")

                        binding.userName.text = ootd.user.nickname

                        // 각 kindId에 해당하는 텍스트가 설정되지 않으면 해당 View를 INVISIBLE로 설정
                        val kindVisibilityMap = mutableMapOf<Int, Boolean>()
                        // 북마크 버튼 리스트와 해당하는 kindId 설정
                        val bookmarkButtons = mapOf(
                            1 to binding.bookmarkOuter,
                            2 to binding.bookmarkTop,
                            3 to binding.bookmarkBottom,
                            4 to binding.bookmarkShoes,
                            5 to binding.bookmarkBag,
                            6 to binding.bookmarkOther
                        )

                        ootd.clothList?.forEach { cloth ->
                            val clothID = cloth.id
                            val categoryName = cloth.categoryName
                            val fitName = cloth.fitName
                            val colorName = cloth.colorName

                            bookmarkButtons[cloth.kindId]?.let { button ->
                                button.tag = cloth.id  // Cloth ID를 버튼의 태그로 저장
                            }

                            // kindId에 따라 텍스트를 설정
                            when (cloth.kindId) {
                                1 -> {
                                    binding.outerText.text = "$categoryName $fitName $colorName"
                                    kindVisibilityMap[1] = true
                                }
                                2 -> {
                                    binding.topText.text = "$categoryName $fitName $colorName"
                                    kindVisibilityMap[2] = true
                                }
                                3 -> {
                                    binding.bottomText.text = "$categoryName $fitName $colorName"
                                    kindVisibilityMap[3] = true
                                }
                                4 -> {
                                    binding.shoesText.text = "$categoryName $colorName"
                                    kindVisibilityMap[4] = true
                                }
                                5 -> {
                                    binding.bagText.text = "$categoryName $fitName $colorName"
                                    kindVisibilityMap[5] = true
                                }
                                6 -> {
                                    binding.otherText.text = "$categoryName $colorName"
                                    kindVisibilityMap[6] = true
                                }
                                else -> Log.d(
                                    "BookmarkOotdFragment",
                                    "Unknown kindId: ${cloth.kindId}"
                                )
                            }
                        }

                        // kindId가 없는 경우 해당 View를 INVISIBLE로 설정
                        if (kindVisibilityMap[1] == null) binding.bookmarkOuter.visibility = View.INVISIBLE
                        if (kindVisibilityMap[2] == null) binding.bookmarkTop.visibility = View.INVISIBLE
                        if (kindVisibilityMap[3] == null) binding.bookmarkBottom.visibility = View.INVISIBLE
                        if (kindVisibilityMap[4] == null) binding.bookmarkShoes.visibility = View.INVISIBLE
                        if (kindVisibilityMap[5] == null) binding.bookmarkBag.visibility = View.INVISIBLE
                        if (kindVisibilityMap[6] == null) binding.bookmarkOther.visibility = View.INVISIBLE
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

    // OOTD 데이터를 반환하는 함수
    private fun getOotdData(): Ootd? {
        return ootdData
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