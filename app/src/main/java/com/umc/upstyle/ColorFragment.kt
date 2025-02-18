package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.databinding.ActivityColorBinding
import com.umc.upstyle.util.ColorUtil

class ColorFragment : Fragment() {

    private var _binding: ActivityColorBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String? = null
    private var selectedSubCategory: String? = null
    private var selectedFit: String? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null


    private var kindId: Int? = null
    private var categoryId: Int? = null
    private var fitId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityColorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 뒤로가기 버튼 클릭 이벤트 설정
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // 이전 액티비티에서 전달된 데이터 수신
        selectedCategory = arguments?.getString("CATEGORY")
        selectedSubCategory = arguments?.getString("SUB_CATEGORY")
        selectedFit = arguments?.getString("FIT")
        selectedSize = arguments?.getString("SIZE")

        kindId = arguments?.getInt("KIND_ID")
        categoryId = arguments?.getInt("CATEGORY_ID")
        fitId = arguments?.getInt("FIT_ID")

        // 옷 종류에 대한 설명
        binding.mainTitleTextView.text = "$selectedCategory"

        // Title 설정
        binding.titleTextView.text = "컬러를 선택하세요"

        setupColorOptions()

        binding.nextButton.setOnClickListener {
            saveAndFinish()
        }
    }

    private fun setupColorOptions() {
        val colors = listOf(
            "블랙" to R.color.black,
            "실버" to R.color.silver,
            "화이트" to R.color.white,
            "그레이" to R.color.gray,
            "레드" to R.color.red,
            "버건디" to R.color.burgundy,
            "핑크" to R.color.pink,
            "오렌지" to R.color.orange,
            "아이보리" to R.color.ivory,
            "오트밀" to R.color.oatmeal,
            "옐로우" to R.color.yellow,
            "그린" to R.color.green,
            "카키" to R.color.khaki,
            "민트" to R.color.mint,
            "스카이블루" to R.color.skyblue,
            "블루" to R.color.blue,
            "네이비" to R.color.navy,
            "퍼플" to R.color.purple,
            "브라운" to R.color.brown,
            "카멜" to R.color.camel,
            "베이지" to R.color.beige,
            "연청" to R.color.light_denim,
            "중청" to R.color.denim,
            "흑청" to R.color.deep_denim,
            "기타색상" to R.color.othercolor
        )

        createButtons(binding.optionsLayout, colors) { selectedOption ->
            selectedColor = selectedOption
            binding.nextButton.setBackgroundResource(R.drawable.next_on) // 버튼 활성화
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<Pair<String, Int>>, onClick: (String) -> Unit) {
        layout.removeAllViews()

        options.forEach { option ->
            // 버튼 내부 레이아웃 생성 (수평)
            val horizontalLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.button_background) // 버튼 배경 설정
                setPadding(20, 20, 20, 20) // 내부 패딩 설정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16) // 버튼 간격 설정
                }
                isClickable = true // 클릭 가능
                isFocusable = true
                setOnClickListener {
                    layout.children.forEach { child ->
                        child.setBackgroundResource(R.drawable.button_background) // 초기화
                    }
                    this.setBackgroundResource(R.drawable.button_background_pressed) // 선택된 버튼 스타일
                    onClick(option.first)
                }
            }

            // 색상 원 생성
            val colorView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                    setMargins(0, 9, 20, 0) // 텍스트와 간격 설정
                }
                background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_background_bk)
                background.setTint(ContextCompat.getColor(requireContext(), option.second)) // 색상 설정
            }

            // 텍스트 추가
            val textView = TextView(requireContext()).apply {
                text = option.first
                setTypeface(null, android.graphics.Typeface.BOLD) // 텍스트를 Bold로 설정
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // 텍스트 색상
            }

            // 색상 원과 텍스트를 레이아웃에 추가
            horizontalLayout.addView(colorView)
            horizontalLayout.addView(textView)

            // FlexboxLayout에 버튼(수평 레이아웃) 추가
            layout.addView(horizontalLayout)
        }
    }

    private fun saveAndFinish() {
        val color = selectedColor // 지역 변수로 값 고정

        if (color.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "컬러를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val colorId = ColorUtil.getColorIdByName(color)


        // 데이터를 Bundle에 추가
        val bundle = Bundle().apply {
            putString("CATEGORY", selectedCategory)
            putString("SUB_CATEGORY", selectedSubCategory)
            putString("FIT", selectedFit)
            putString("SIZE", selectedSize)
            putString("COLOR", color)



            kindId?.let { putInt("KIND_ID", it) }
            categoryId?.let { putInt("CATEGORY_ID", it) }
            fitId?.let { putInt("FIT_ID", it) }
            if (colorId != null) {
                putInt("COLOR_ID", colorId)
            }

        }

        val action = R.id.etcFragment

        // 네비게이션 액션 수행
        findNavController().navigate(action, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
