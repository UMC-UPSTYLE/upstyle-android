package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.upstyle.data.model.ResponseDetailResponse
import com.umc.upstyle.data.network.RequestService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentBottomSheetViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BottomSheetViewFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetViewBinding? = null
    private val binding get() = _binding!!

    private val responseService: RequestService by lazy {
        RetrofitClient.createService(RequestService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetViewBinding.inflate(inflater, container, false)

        val commentId = arguments?.getInt("commentId") ?: -1
        if (commentId != -1) {
            fetchResponseDetail(commentId)
        }

        return binding.root
    }

    private fun fetchResponseDetail(responseId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    responseService.getResponseDetail(responseId)
                }
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.isSuccess) {
                            apiResponse.result?.let {
                                updateUI(it)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(response: ResponseDetailResponse) {
        binding.tvUsername.text = response.user.nickname
        binding.addInfo.text = response.body

        for (cloth in response.clothResponseList)
            when (cloth.kindId) {
                1 -> binding.tvOuter.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                2 -> binding.tvTop.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                3 -> binding.tvBottom.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                4 -> binding.tvShoes.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                5 -> binding.tvBag.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
                6 -> binding.tvOther.text = "${cloth.categoryName} ${cloth.fitName} ${cloth.colorName}"
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CodiBottomModalSheet"

        fun newInstance(commentId: Int): BottomSheetViewFragment {
            return BottomSheetViewFragment().apply {
                arguments = Bundle().apply {
                    putInt("commentId", commentId)
                }
            }
        }
    }
}
