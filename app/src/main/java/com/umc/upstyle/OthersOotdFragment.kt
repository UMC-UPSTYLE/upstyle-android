package com.umc.upstyle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.umc.upstyle.data.Item
import com.umc.upstyle.databinding.FragmentOthersOotdBinding

// 이미지, 옷 정보, 사용자 이름 받아서
// 1. 이미지 추가
// 2. 옷 정보 추가
// 3. 사용자 이름 추가

class OthersOotdFragment : Fragment() {

    // ViewBinding 객체 선언
    private var _binding: FragmentOthersOotdBinding? = null
    private val binding get() = _binding!!

    private var userName: String? = null // 사용자 이름
    private var selectedCategory: String? = null // OUTER, TOP, BOTTOM 등
    private var selectedSubCategory: String? = null // selectedCategory의 카테고리
    private var selectedFit: String? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentOthersOotdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // View Binding을 통해 BottomNavigationView 숨기기
        val mainBinding = (activity as MainActivity).binding // MainActivity의 View Binding 객체 참조
        mainBinding.bottomNavigationView.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        // View Binding을 통해 BottomNavigationView 다시 보이기
        val mainBinding = (activity as MainActivity).binding // MainActivity의 View Binding 객체 참조
        mainBinding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 가기 버튼 설정
        binding.backButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 사용자 이름 받기
        binding.userName.text = "$userName"

        // 북마크
        var isBookmarked = false

        binding.bookmarkImg.setOnClickListener {
            if (isBookmarked) { binding.bookmarkImg.setBackgroundResource(R.drawable.bookmark_off) }
            else { binding.bookmarkImg.setBackgroundResource(R.drawable.bookmark_on) }
            isBookmarked = !isBookmarked
        }

        // 데이터 리스트
        val items = listOf(
            Item(R.drawable.text_outer, selectedSubCategory ?: "무스탕", selectedFit ?: "오버핏", null, selectedColor ?: "블랙"),
            Item(R.drawable.text_top, selectedSubCategory ?: "니트", selectedFit ?: "레귤러", null, selectedColor ?: "그레이"),
            Item(R.drawable.text_bottom, selectedSubCategory ?: "숏팬츠", selectedFit ?: "슬림",null, selectedColor ?: "블랙"),
            Item(R.drawable.text_bag, selectedSubCategory ?: "숄더백", selectedSize ?: "미디엄", null,selectedColor ?: "블랙"),
            Item(R.drawable.text_shoes, selectedSubCategory ?: "부츠/워커", null, null, selectedColor ?: "블랙"),
            Item(R.drawable.text_other, selectedSubCategory ?: "" , null, null, selectedColor ?: "")
        )

        // 데이터 리스트를 기반으로 동적으로 뷰 생성
        for (item in items) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16.dpToPx() // 각 행의 간격
                }
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 0) // 패딩을 없앰
            }

            val iconView = ImageView(requireContext()).apply {
                setImageResource(item.iconRes)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        marginStart = 0 // 왼쪽에 딱 붙도록 간격 제거
                        marginEnd = 16.dpToPx() // 아이콘과 텍스트 간 간격
                }
            }

            val textView = TextView(requireContext()).apply {
                text = buildString {
                    append(item.subCategory) // subCategory 추가
                    item.fit?.let { append(" $it") } // fit 추가 (null이 아니면 공백 후 추가)
                    item.size?.let { append(" $it") } // size 추가 (null이 아니면 공백 후 추가)
                    append(" ${item.color}") // color 항상 추가
                }
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // 아이템을 부모 레이아웃에 추가
            rowLayout.addView(iconView)
            rowLayout.addView(textView)
            binding.parentLayout.addView(rowLayout) // ViewBinding 사용
        }
    }

    // dp 값을 px로 변환하는 함수
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수를 방지하기 위해 ViewBinding 해제
    }
}