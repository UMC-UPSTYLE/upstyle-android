package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.model.Ootd
import com.umc.upstyle.data.network.OotdApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentOotdDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OotdDetailFragment : Fragment() {
    private var _binding: FragmentOotdDetailBinding? = null
    private val binding get() = _binding!!
    private val apiService = RetrofitClient.createService(OotdApiService::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOotdDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 전달된 정보 받기
        val ootdId = arguments?.getInt("SELECTED_OOTD_ID")
        val ootdImage = arguments?.getString("SELECTED_OOTD_IMAGE")

        if (!ootdImage.isNullOrEmpty()) {
            Log.e("PIC", "Using provided image: $ootdImage")
            binding.photoImageView.tag = ootdImage
            displayImage(ootdImage)
        }

        if (ootdId != null) {
            fetchOotdData(ootdId)
        }
    }

    // 서버에서 데이터 가져오기
    private fun fetchOotdData(ootdId: Int) {
        apiService.getOOTDById(ootdId).enqueue(object : Callback<ApiResponse<Ootd>> {
            override fun onResponse(call: Call<ApiResponse<Ootd>>, response: Response<ApiResponse<Ootd>>) {
                if (response.isSuccessful) {
                    response.body()?.result?.let { ootd ->
                        Log.e("API_RESPONSE", "Fetched OOTD: $ootd")
                        displayOotdData(ootd)
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

    // ootd 정보 텍스트로 나열
    private fun displayOotdData(ootd: Ootd) {
        binding.date.text = formatDateKey(ootd.date)

        val textViewList = listOf(
            binding.outerText,
            binding.topText,
            binding.bottomText,
            binding.shoesText,
            binding.bagText,
            binding.otherText
        )

        if (ootd.clothList.isNotEmpty()) {
            for (i in ootd.clothList.indices) {
                if (i < textViewList.size) {
                    val cloth = ootd.clothList[i]
                    textViewList[i].text =
                        "${cloth.kindName} ${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                }
            }
        }

        for (i in ootd.clothList.size until textViewList.size) {
            textViewList[i].text = "옷에 대한 정보가 없습니다"
        }
    }

    private fun formatDateKey(dateString: String): String {
        val parts = dateString.split("-")
        return parts[1] + parts[2]
    }

    private fun displayImage(imageUrl: String) {
        Log.e("DEBUG", "imageUrl: $imageUrl")
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 문제 해결
            .placeholder(R.drawable.bg_other_day) // 로딩 중 기본 이미지
            .error(R.drawable.bg_other_day) // 로딩 실패 시 기본 이미지
            .into(binding.photoImageView)

        binding.photoImageView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}