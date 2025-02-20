package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.umc.upstyle.databinding.FragmentChatBinding

class ChatFragment : Fragment(R.layout.fragment_chat), VoteFragmentListener, RequestFragmentListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TabPagerAdapter(this)
        val voteFragment = VoteFragment()
        val requestFragment = RequestFragment()

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        // 리스너 설정
        voteFragment.setVoteFragmentListener(this)
        requestFragment.setRequestFragmentListener(this)

        adapter.addFragment(voteFragment)   // 첫 번째 탭: 투표
        adapter.addFragment(requestFragment) // 두 번째 탭: 코디 요청

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2  // 탭 개수만큼 설정


        // TabLayout과 ViewPager2를 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "투표"
                1 -> tab.text = "코디 요청"
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.btnWritePost.setOnClickListener {
                            findNavController().navigate(R.id.createVoteFragment)
                        }
                    }
                    1 -> {
                        binding.btnWritePost.setOnClickListener {
                            findNavController().navigate(R.id.createRequestFragment)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnWritePost.setOnClickListener {
            findNavController().navigate(R.id.createVoteFragment)
        }
    }

    // VoteFragment에서 클릭된 데이터 받아서 PostDetailFragment로 이동
    override fun onVoteSelected(postId: Int, postTitle: String, voteCount: Int) {
        val action = ChatFragmentDirections
            .actionChatFragmentToPostDetailFragment(postId, postTitle, voteCount)
        findNavController().navigate(action)
    }

    // RequestFragment에서 클릭된 데이터 받아서 RequestDetailFragment로 이동
    override fun onRequestSelected(requestId: Int, requestTitle: String, commentCount: Int) {
        val action = ChatFragmentDirections
            .actionChatFragmentToRequestDetailFragment(requestId, requestTitle, commentCount)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        binding.viewPager.requestLayout()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

