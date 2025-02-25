package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.VoteDetailResponse
import com.umc.upstyle.data.model.VoteOption
import com.umc.upstyle.data.model.VoteResponseRequest
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.VoteService
import com.umc.upstyle.databinding.FragmentPostDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var voteService: VoteService
    private val args: PostDetailFragmentArgs by navArgs()

    private lateinit var voteOptionAdapter: VoteOptionAdapter
    private var selectedOptionId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        voteService = RetrofitClient.createService(VoteService::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val voteId = args.id // 전달된 voteId

        setupRecyclerView()
        fetchVoteDetails(voteId)

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.btnVote.setOnClickListener {
            selectedOptionId?.let { optionId ->
                submitVoteResponse(voteId, optionId)
            } ?: Toast.makeText(requireContext(), "옵션을 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        // isVoteCompleted는 기본값 false로 전달되고, onItemSelected는 올바른 타입으로 전달됨
        voteOptionAdapter = VoteOptionAdapter(emptyList(), { selectedOption ->
            selectedOptionId = selectedOption.id  // VoteOption 객체에서 id를 가져옵니다.
        }, isVoteCompleted = false)  // 투표 완료 상태는 false로 전달

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = voteOptionAdapter
            isNestedScrollingEnabled = false
        }
    }



    private fun fetchVoteDetails(voteId: Int) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { voteService.getVoteDetail(voteId) }
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.isSuccess) {
                            updateUI(apiResponse.result)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(voteDetail: VoteDetailResponse) {
        binding.tvUsername.text = voteDetail.user.nickname
        binding.tvTitle.text = voteDetail.title
        binding.tvText.text = voteDetail.body
        Glide.with(this)
            .load(voteDetail.imageUrl)
            .into(binding.ivImage)

        if(voteDetail.imageUrl=="") {
            binding.ivImage.visibility = View.GONE
        }

        voteDetail.optionList?.let { options ->
            val voteOptions = options.map { option ->
                VoteOption(option.id, option.clothId, option.imageUrl, option.name, option.responseCount)
            }
            voteOptionAdapter.setItems(voteOptions)
        }
    }

    private fun submitVoteResponse(voteId: Int, selectedOptionId: Int) {
        val requestBody = VoteResponseRequest(userId = 1, optionId = selectedOptionId)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { voteService.submitVoteResponse(voteId, requestBody) }
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.isSuccess) {
                            updateVoteCounts(apiResponse.result.voteResultList)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateVoteCounts(voteResultList: List<VoteOption>) {
        val updatedOptions = voteResultList.map { result ->
            VoteOption(result.id, result.clothId, result.imageUrl, result.name, result.responseCount)
        }

        binding.btnVote.setBackgroundResource(R.drawable.bg_gray_10)
        binding.btnVote.isClickable = false
        binding.btnVote.isFocusable = false
        binding.tvVote.text = "투표 완료"

        // 투표 완료 상태를 adapter에 전달
        voteOptionAdapter.setVoteCompleted(true)

        voteOptionAdapter.setItems(updatedOptions)
        binding.recyclerView.post {
            updatedOptions.forEachIndexed { index, option ->
                val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(index) as? VoteOptionAdapter.ViewHolder
                viewHolder?.let {
                    it.itemView.findViewById<TextView>(R.id.tv_option_vote_count).apply {
                        text = "${option.responseCount}명"
                        visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}