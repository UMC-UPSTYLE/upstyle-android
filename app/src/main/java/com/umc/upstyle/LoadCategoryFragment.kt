package com.umc.upstyle

import android.graphics.drawable.Drawable
import com.umc.upstyle.utils.Item_load
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.viewmodel.CategoryViewModel
import com.umc.upstyle.databinding.FragmentLoadCategoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadCategoryFragment : Fragment() {
    private var _binding: FragmentLoadCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val args: LoadCategoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val position = args.position // 전달받은 position 값
        val username = args.username // 전달받은 username
//        Toast.makeText(context, "받아온 포지션: $position", Toast.LENGTH_SHORT).show()
        categoryViewModel.position = position
        Log.d("LoadCategoryFragment", "받아온 포지션: ${categoryViewModel.position}")
        categoryViewModel.username = username
        _binding = FragmentLoadCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LoadCategoryFragment에서 데이터 받기
        findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM")?.let { description ->
            findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM_IMAGE_URL")?.let { imageUrl ->
                findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("CATEGORY")?.let { category ->
                    findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("CLOTH_ID")?.let { clothId ->

                    val kindId = arguments?.getInt("KIND_ID") ?:0
                    val categoryId = arguments?.getInt("CATEGORY_ID") ?:0
                    val fitId = arguments?.getInt("FIT_ID") ?:0
                    val colorId = arguments?.getInt("COLOR_ID") ?:0
                    val addInfo = arguments?.getString("ADD_INFO") ?:""

                    // 이제 description과 imageUrl을 사용해서 필요한 작업을 처리
                    val item = Item_load(description, imageUrl,false, clothId, kindId, categoryId, fitId, colorId, addInfo) // 아이템 객체 생성

                    // 직전 프래그먼트로 데이터 전달하면서 navigateUp
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM", description)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM_IMAGE_URL", imageUrl)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("CATEGORY", category)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("CLOTH_ID", clothId)
//                    Toast.makeText(context, "로드카테고리에서 전송 포지션: ${categoryViewModel.position}", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("POSITION", categoryViewModel.position)

                    findNavController().navigateUp()
                    }
                }
            }
        }

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach { key ->
            val value = findNavController().previousBackStackEntry?.savedStateHandle?.get<Any>(key)
            when (value) {
                is String -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(key)
                is Int -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Int>(key)
                is Boolean -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Boolean>(key)
                is Float -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Float>(key)
                is Double -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Double>(key)
                else -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Any>(key)
            }
        }

        binding.tvUsername.text = categoryViewModel.username



        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OUTER", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoTop.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "TOP", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoBottom.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BOTTOM", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoShoes.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "SHOES", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoBag.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BAG", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoOther.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OTHER", userId = -1)
            findNavController().navigate(action)
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



    override fun onDestroyView() {

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach {
            findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(it)
        }
        super.onDestroyView()
        _binding = null // ViewBinding 해제
    }
}
