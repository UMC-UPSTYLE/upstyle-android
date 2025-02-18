package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.model.ClothIdResponse
import com.umc.upstyle.data.model.ClothPreview
import com.umc.upstyle.data.model.ClothRequestDTO
import com.umc.upstyle.data.model.ClothRequestDesDTO
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.viewmodel.ClothViewModel
import com.umc.upstyle.data.viewmodel.ResponseViewModel
import com.umc.upstyle.databinding.FragmentResponseBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResponseFragment : Fragment() {
    private var _binding: FragmentResponseBinding? = null
    private val binding get() = _binding!!

    private val responseViewModel: ResponseViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResponseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // clothList를 감지
        responseViewModel.clothList.observe(viewLifecycleOwner) { clothList ->
            // clothList에 값이 하나라도 있으면 btnSave의 배경 색을 변경
            if (clothList.isNotEmpty()) {
                binding.btnSave.setBackgroundResource(R.drawable.bg_circle_yellow) // 원하는 색으로 변경
                binding.btnSave.isFocusable = true
                binding.btnSave.isClickable = true
            }
        }

        val userId = arguments?.getInt("USER_ID") ?:2

        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getUserCloset(userId).enqueue(object : Callback<ClosetResponse> {
            override fun onResponse(call: Call<ClosetResponse>, response: Response<ClosetResponse>) {
                if (response.isSuccessful) {
                    val userName = response.body()?.result?.userName
                    binding.tvUsername.text = "${userName}"

                } else {
                    binding.tvUsername.text = "오류"
                }
            }

            override fun onFailure(call: Call<ClosetResponse>, t: Throwable) {
                binding.tvUsername.text = "API 실패"

            }
        })

        observeSelectedItem()

        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "OUTER")
            findNavController().navigate(action)
        }

        binding.btnGoTop.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "TOP")
            findNavController().navigate(action)
        }

        binding.btnGoBottom.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "BOTTOM")
            findNavController().navigate(action)
        }

        binding.btnGoShoes.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "SHOES")
            findNavController().navigate(action)
        }

        binding.btnGoBag.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "BAG")
            findNavController().navigate(action)
        }

        binding.btnGoOther.setOnClickListener {
            val action = ResponseFragmentDirections.actionResponseFragmentToLoadItemFragment(category = "OTHER")
            findNavController().navigate(action)
        }

        binding.btnSave.setOnClickListener {
            sendSelectedItemToPreviousFragment(responseViewModel.clothList.value, responseViewModel.clothIDList.value)
        }

    }

    private fun sendSelectedItemToPreviousFragment(
        selectedItem: MutableList<ClothRequestDesDTO>?,
        selectedItemID: MutableList<ClothIdResponse>?
    ) {
        // selectedItemID가 null일 경우 빈 리스트로 처리
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "SELECTED_ITEM", selectedItem ?: mutableListOf()
        )

        // selectedItemID가 null일 경우 빈 리스트로 처리
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "SELECTED_ITEM_ID", selectedItemID ?: mutableListOf()
        )

        responseViewModel.clearData() // 필요한 메서드를 호출해서 ViewModel의 데이터를 초기화
        findNavController().popBackStack()
    }


    private fun observeSelectedItem() {
        val navBackStackEntry = findNavController().currentBackStackEntry
        navBackStackEntry?.savedStateHandle?.getLiveData<String>("SELECTED_ITEM")?.observe(
            viewLifecycleOwner
        ) { selectedItem ->
            val category = navBackStackEntry.savedStateHandle.get<String>("CATEGORY")
//
            // ViewModel에 데이터 추가
            val clothId = navBackStackEntry.savedStateHandle.get<Int>("CLOTH_ID") ?: 0
            val kindId = navBackStackEntry.savedStateHandle.get<Int>("KIND_ID") ?: 0
            val categoryId = navBackStackEntry.savedStateHandle.get<Int>("CATEGORY_ID") ?: 0
            val fitId = navBackStackEntry.savedStateHandle.get<Int>("FIT_ID") ?: 0
            val colorId = navBackStackEntry.savedStateHandle.get<Int>("COLOR_ID") ?: 0
            val addInfo = navBackStackEntry.savedStateHandle.get<String>("ADD_INFO") ?: ""
            val description = navBackStackEntry.savedStateHandle.get<String>("SELECTED_ITEM") ?: ""


            if(clothId != 0) {
                // DTO 생성
                val clothRequestDTO = ClothRequestDesDTO(
                    clothId = clothId,
                    clothKindId = kindId,
                    clothCategoryId = categoryId,
                    fitCategoryId = fitId,
                    colorCategoryId = colorId,
                    additionalInfo = addInfo,
                    description = description
                )

                responseViewModel.addClothRequest(clothRequestDTO)

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ViewBinding 해제
    }
}
