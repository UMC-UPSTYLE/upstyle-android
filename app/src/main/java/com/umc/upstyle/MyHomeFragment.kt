package com.umc.upstyle

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.databinding.ActivityMyhomeBinding
import com.umc.upstyle.data.model.ClosetResponse
import com.umc.upstyle.data.model.Gender
import com.umc.upstyle.data.model.OOTDCalendar
import com.umc.upstyle.data.model.OOTDPreview
import com.umc.upstyle.data.network.OotdApiService
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MyHomeFragment : Fragment() {

    private var _binding: ActivityMyhomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var monthListManager: LinearLayoutManager
    private val center = Int.MAX_VALUE / 2 // 중앙 위치 값 (Adapter에서 사용한 값)
    private val apiService = RetrofitClient.createService(OotdApiService::class.java)
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityMyhomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout2)

        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)

        val apiService = RetrofitClient.createService(ApiService::class.java)

        userApiService.getUserInfo("Bearer $jwtToken").enqueue(object :
            Callback<ApiResponse<AccountInfoDTO>> {
            override fun onResponse(
                call: Call<ApiResponse<AccountInfoDTO>>,
                response: Response<ApiResponse<AccountInfoDTO>>
            ) {
                if (response.isSuccessful) {
                    val nickName = response.body()?.result?.nickname
                    binding.topUserName.text = "${nickName} 님"
                    binding.wardrobeText.text = "${nickName}님의 옷장"
                } else {
                    binding.wardrobeText.text = "닉네임을 불러올 수 없음"
                    binding.topUserName.text = "닉네임을 불러올 수 없음"
                }

            }

            override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {
                Log.e("Account", "사용자 정보 요청 실패: ${t.message}")
            }
        })


        // 리사이클러뷰 설정
        monthListManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val monthListAdapter = AdapterMonth()

        binding.calendarCustom.apply {
            layoutManager = monthListManager
            adapter = monthListAdapter
            scrollToPosition(center) // 중앙으로 스크롤 초기화
        }

        val snap = PagerSnapHelper()
        snap.attachToRecyclerView(binding.calendarCustom)

        // 초기 년/월 설정
        updateYearMonth()

        // 스크롤 이벤트 감지
        binding.calendarCustom.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 스크롤이 멈췄을 때
                    updateYearMonth()
                }
            }
        })

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // MONTH는 0부터 시작하므로 1을 더함

        binding.calendarMonth.text = "${year}년 ${month}월"

        fetchOOTDCalendarData(userId = 1, year, month)

        // SwipeRefreshLayout의 새로고침 리스너 설정
        swipeRefreshLayout.setOnRefreshListener {
            // 새로고침 작업을 실행, 예: 데이터 로드
            fetchOOTDCalendarData(userId = 1, year, month)
        }

        // 섹션 이동 버튼 리스너
        binding.closetSection.setOnClickListener {
            findNavController().navigate(R.id.closetFragment)
        }
        binding.todayOOTDSection.setOnClickListener {
            findNavController().navigate(R.id.todayOotdFragment)
        }

    }

    // OOTD 캘린더 데이터 가져오기
    private fun fetchOOTDCalendarData(userId: Int, year: Int, month: Int) {
        apiService.getOOTDCalendar(userId, year, month).enqueue(object :
            Callback<ApiResponse<OOTDCalendar>> {
            override fun onResponse(
                call: Call<ApiResponse<OOTDCalendar>>,
                response: Response<ApiResponse<OOTDCalendar>>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        Log.d("API_SUCCESS", "Response: ${responseData}")
                    }

                    response.body()?.result?.ootdPreviewList?.let { ootdList ->
                        updateCalendarWithOOTD(ootdList)
                    }
                } else {
                    Log.e("API_ERROR", "Response Failed: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ApiResponse<OOTDCalendar>>, t: Throwable) {
                Log.e("API_ERROR", "Request Failed", t)
            }
        })
        // 데이터 로드가 끝났을 때 새로고침 상태를 종료
        swipeRefreshLayout.isRefreshing = false // 새로고침 종료
    }

    // 데이터를 Adapter로 전달하여 UI 업데이트
    private fun updateCalendarWithOOTD(ootdList: List<OOTDPreview>) {
        val adapter = binding.calendarCustom.adapter as? AdapterDay
        adapter?.updateOOTDData(ootdList)
    }

    private fun updateYearMonth() {
        // RecyclerView에서 현재 중앙에 위치한 아이템의 위치를 가져옴
        val currentPosition = monthListManager.findFirstVisibleItemPosition()

        // 위치가 유효하지 않거나 첫 번째 위치일 경우 예외 처리
        if (currentPosition == RecyclerView.NO_POSITION || currentPosition == 0) return

        // 중앙 위치를 기준으로 날짜를 계산
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1) // 매달의 첫 번째 날로 설정
            add(Calendar.MONTH, currentPosition - center) // 중앙 위치로부터 몇 번째 월인지 계산
        }

        // 계산된 년/월을 UI에 반영
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // MONTH는 0부터 시작하므로 1을 더함
        binding.calendarMonth.text = "${year}년 ${month}월"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
