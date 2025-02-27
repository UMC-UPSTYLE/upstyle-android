package com.umc.upstyle

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 공통 클릭 이벤트 처리
        binding.btnGoToOuter.setOnClickListener { navigateToSearchItemFragment("OUTER", 1) }
        binding.btnGoToTop.setOnClickListener { navigateToSearchItemFragment("TOP", 2) }
        binding.btnGoToBottom.setOnClickListener { navigateToSearchItemFragment("BOTTOM", 3) }
        binding.btnGoToShoes.setOnClickListener { navigateToSearchItemFragment("SHOES", 4) }
        binding.btnGoToBag.setOnClickListener { navigateToSearchItemFragment("BAG", 5) }
        binding.btnGoToOther.setOnClickListener { navigateToSearchItemFragment("OTHER", 6) }

        binding.categoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.subcategoryfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.fitsizefilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }
        binding.colorfilterbtn.setOnClickListener { findNavController().navigate(R.id.searchCategoryFragment) }

//        setBack()

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun setBack() {
        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getMyCloset().enqueue(object : Callback<ClosetResponse> {
            override fun onResponse(call: Call<ClosetResponse>, response: Response<ClosetResponse>) {
                if (response.isSuccessful) {
                    val closetResponse = response.body()
                    closetResponse?.let {
                        if (it.isSuccess) {
                            val clothKindList = it.result.clothKindList
                            for (clothKind in clothKindList) {
                                when (clothKind.kindId) {
                                    1 -> loadThumbnail(binding.ivOuter, clothKind.thumbnailUrl)
                                    2 -> loadThumbnail(binding.ivTop, clothKind.thumbnailUrl)
                                    3 -> loadThumbnail(binding.ivBottom, clothKind.thumbnailUrl)
                                    4 -> loadThumbnail(binding.ivShoes, clothKind.thumbnailUrl)
                                    5 -> loadThumbnail(binding.ivBag, clothKind.thumbnailUrl)
                                    6 -> loadThumbnail(binding.ivOther, clothKind.thumbnailUrl)
                                }
                            }
                        } else {
                            Log.e("API_ERROR", "서버 응답 실패: ${it.message}")
                        }
                    }
                } else {
                    Log.e("API_ERROR", "HTTP 실패: ${response.code()}, ${response.errorBody()?.string()}")
                }
            }


            override fun onFailure(call: Call<ClosetResponse>, t: Throwable) {
                Log.e("API_ERROR", "옷장 데이터 불러오기 실패: ${t.message}")
            }
        })
    }

    // 썸네일을 ImageView의 src로 설정하는 함수
    private fun loadThumbnail(imageView: ImageView, imageUrl: String) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    imageView.setImageDrawable(resource) // ImageView에 이미지를 설정
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    imageView.setImageDrawable(placeholder) // 로드된 이미지가 없을 때 placeholder 설정
                }
            })
    }

    // Navigation Component를 통한 전환 함수
    private fun navigateToSearchItemFragment(category: String, kindId: Int) {
        val action = SearchFragmentDirections
            .actionSearchFragmentToSearchItemFragment(category, kindId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수 방지: 뷰가 파괴되면 바인딩 해제
        _binding = null
    }
}
