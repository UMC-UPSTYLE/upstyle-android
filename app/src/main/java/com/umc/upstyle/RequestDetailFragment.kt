package com.umc.upstyle

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.ClothIdResponse
import com.umc.upstyle.data.model.ClothRequestDTO
import com.umc.upstyle.data.model.ClothRequestDesDTO
import com.umc.upstyle.data.model.CodiResPreview
import com.umc.upstyle.data.model.RequestDetailResponse
import com.umc.upstyle.data.network.RequestService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.databinding.FragmentRequestDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestDetailFragment : Fragment() {
    private var _binding: FragmentRequestDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var responseAdapter: ResponseAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var codiResPreviewList = mutableListOf<CodiResPreview>()


    private var requestId: Int = 0
    private var userId: Int = 0
    private var commentCount: Int = 0
    private lateinit var requestService: RequestService
    private val args: RequestDetailFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestDetailBinding.inflate(inflater, container, false)
        requestService = RetrofitClient.createService(RequestService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        requestId = args.id // 이전 프래그먼트에서 전달된 voteId
        commentCount = args.commentCount

        fetchRequestDetails(requestId)

        // SwipeRefreshLayout의 새로고침 리스너 설정
        swipeRefreshLayout.setOnRefreshListener {
            // 새로고침 작업을 실행, 예: 데이터 로드
            fetchRequestDetails(requestId)
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        responseAdapter = ResponseAdapter(codiResPreviewList) { commentId ->
            showBottomSheet(commentId) // 아이템 클릭 시 BottomSheet 호출
        }

        recyclerView.adapter = responseAdapter


        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.btnPlus.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("USER_ID", userId)
            }
            val action = RequestDetailFragmentDirections.actionRequestDetailFragmentToResponseFragment(userId)
            findNavController().navigate(action)

        }
    }


    override fun onResume() {
        super.onResume()

        fetchRequestDetails(requestId)

        val clothList = findNavController().currentBackStackEntry?.savedStateHandle?.get<MutableList<ClothRequestDesDTO>>("SELECTED_ITEM")?: null
        val clothIDList = findNavController().currentBackStackEntry?.savedStateHandle?.get<MutableList<ClothIdResponse>>("SELECTED_ITEM_ID")?: null

        if((clothIDList != null) && (clothList != null)) {
            createBottomSheet(clothList, clothIDList, requestId)
        }
    }


    private fun fetchRequestDetails(requestId: Int) {
        lifecycleScope.launch {
            try {
                Log.d("RequestDetail", "API 호출 시작: requestId = $requestId")

                val response = withContext(Dispatchers.IO) { requestService.getRequestDetail(requestId) }

                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        Log.d("RequestDetail", "API 응답 성공: ${apiResponse.result}")

                        if (apiResponse.isSuccess) {
                            updateUI(apiResponse.result)

                            userId = apiResponse.result.user.id
                            // 🚨 Null 체크 추가
                            val responseList = apiResponse.result.codiResPreviewList ?: emptyList()

                            codiResPreviewList.clear()
                            codiResPreviewList.addAll(responseList)

                            Log.d("RequestDetail", "업데이트된 리스트 크기: ${codiResPreviewList.size}")

                            responseAdapter.notifyDataSetChanged()

                            // 데이터 로드가 끝났을 때 새로고침 상태를 종료
                            swipeRefreshLayout.isRefreshing = false // 새로고침 종료
                        }
                    } ?: Log.e("RequestDetail", "응답 본문이 null")
                } else {
                    Log.e("RequestDetail", "API 응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RequestDetail", "API 요청 실패: ${e.message}")
            }
        }
    }




    private fun updateUI(requestDetail: RequestDetailResponse) {
        binding.tvUsername.text = requestDetail.user.nickname
        binding.tvTitle.text = requestDetail.title
        binding.tvText.text = requestDetail.body
        binding.tvCommentCount.text = commentCount.toString()
        Glide.with(this)
            .load(requestDetail.imageUrl)
            .into(binding.ivImage)

    }

    private fun showBottomSheet(commentId: Int) {
        // 클릭된 댓글 id를 Bottom Sheet에 넘겨서 API 호출
        val bottomSheetViewFragment = BottomSheetViewFragment.newInstance(commentId)
        // 모서리 둥글게 해서 Bottom Sheet 불러오는 코드
        bottomSheetViewFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        bottomSheetViewFragment.show(parentFragmentManager, bottomSheetViewFragment.tag)
    }

    private fun createBottomSheet(clothList: MutableList<ClothRequestDesDTO>, clothIDList: MutableList<ClothIdResponse>, requestId: Int) {
        // 클릭된 댓글 id를 Bottom Sheet에 넘겨서 API 호출
        val codiBottomSheetDialogFragment = CodiBottomSheetFragment.newInstance(clothList, clothIDList, requestId)
        // 모서리 둥글게 해서 Bottom Sheet 불러오는 코드
        codiBottomSheetDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        codiBottomSheetDialogFragment.show(parentFragmentManager, codiBottomSheetDialogFragment.tag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

