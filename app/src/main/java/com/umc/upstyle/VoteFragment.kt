package com.umc.upstyle

import PostAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.upstyle.data.repository.PostRepository
import com.umc.upstyle.databinding.FragmentVoteBinding
import com.umc.upstyle.data.model.Post


class VoteFragment : Fragment() {
    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private var listener: VoteFragmentListener? = null

    // ChatFragment에서 리스너 설정
    fun setVoteFragmentListener(listener: VoteFragmentListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 서버에서 데이터 가져오기
        PostRepository.fetchPosts { postList ->
            val adapter = PostAdapter(postList) { post ->
                listener?.onVoteSelected(post.id, post.title, post.totalResponseCount)
                Log.d("Retrofit", "id 전달 성공!")
            }

            binding.voteRecyclerView.adapter = adapter
            binding.voteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()

        PostRepository.fetchPosts { postList ->
            binding.voteRecyclerView.adapter = PostAdapter(postList) { post ->
                listener?.onVoteSelected(post.id, post.title, post.totalResponseCount)
                Log.d("Retrofit", "id 전달 성공!")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 이벤트 전달을 위한 인터페이스
interface VoteFragmentListener {
    fun onVoteSelected(postId: Int, postTitle: String, voteCount: Int)
}