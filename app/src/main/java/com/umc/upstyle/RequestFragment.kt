package com.umc.upstyle

import RequestAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.upstyle.data.repository.RequestRepository
import com.umc.upstyle.databinding.FragmentRequestBinding
import com.umc.upstyle.data.model.Request

// 이벤트 전달을 위한 인터페이스
interface RequestFragmentListener {
    fun onRequestSelected(requestId: Int, requestTitle: String, commentCount: Int)
}

class RequestFragment : Fragment() {
    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!
    private var listener: RequestFragmentListener? = null


    // 리스너 설정 함수
    fun setRequestFragmentListener(listener: RequestFragmentListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 서버에서 데이터 가져오기
        RequestRepository.fetchRequests { requestList ->
            Log.d("RequestFragment", "받아온 리스트 크기: ${requestList.size}") // 리스트 크기 확인

            val adapter = RequestAdapter(requestList) { request ->
                Log.d("RequestFragment", "아이템 클릭됨: ${request.id}, ${request.title}")

                if (listener == null) {
                    Log.e("RequestFragment", "listener가 null 상태입니다!")

                } else {
                    listener?.onRequestSelected(request.id, request.title, request.commentCount)
                    Log.d("RequestFragment", "onRequestSelected 호출됨")
                }
            }

            binding.recyclerView.adapter = adapter
        }

    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
