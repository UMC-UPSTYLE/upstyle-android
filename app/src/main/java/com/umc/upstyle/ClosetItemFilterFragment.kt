package com.umc.upstyle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.databinding.FragmentClosetItemFilterBinding
class ClosetItemFilterFragment : Fragment() { // 주석 처리한 코드는 이전 구현 때 사용했던 코드들인데 다음 기능을 추가할 때 필요할 수 있는 코드들이라 삭제하지 않은 코드들입니다.

    private var _binding: FragmentClosetItemFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var category: String
    private lateinit var ClosetItemFragment: String

//    // 선택된 필터 정보를 저장하는 변수
//    private var selectedCategory: String? = null
//    private var selectedSubCategory: String? = null
//    private var selectedFit: String? = null
//    private var selectedSize: String? = null
//    private var selectedColor: String? = null

    // 선택된 색깔 필터 정보를 저장하는 변수
    private var filteredColor: String? = null

    // 선택된 옵션을 관리하는 MutableSet
    private val selectedOptions = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClosetItemFilterBinding.inflate(inflater, container, false)

        category = arguments?.getString("category").toString() // 전달된 데이터 수신
        ClosetItemFragment = arguments?.getString("ClosetItemFragment").toString()

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

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 이전 Fragment로 이동
        binding.backButton.setOnClickListener {
            val ClosetItemFragment = arguments?.getString("ClosetItemFragment")

            when (ClosetItemFragment) {
                "true" -> findNavController().navigateUp()
                else -> findNavController().navigate(R.id.SearchItemFragment) // 기본적으로 이전 화면으로 이동
            }
        }

//        // 이전 화면에서 전달된 데이터 수신
//        receiveArguments()

        // 컬러 옵션, 옵션 초기화
        setupColorOptions()

        // 완료 버튼 클릭 처리
        binding.compOffButton.setOnClickListener { saveAndFinish() }
    }

//    private fun receiveArguments() {
//        arguments?.let {
//            selectedCategory = it.getString("CATEGORY")
//            selectedSubCategory = it.getString("SUB_CATEGORY")
//            selectedFit = it.getString("FIT")
//            selectedSize = it.getString("SIZE")
//        }
//    }

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

        createButtons(binding.optionsLayout, colors)

        // 초기화 버튼 클릭 처리
        binding.resetOffButton.setOnClickListener {
            resetSelections(binding.optionsLayout)
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<Pair<String, Int>>) {
        layout.removeAllViews()

        options.forEach { option ->
            val button = createOptionButton(option)
            layout.addView(button)
        }
    }

    private fun createOptionButton(option: Pair<String, Int>): View {
        val horizontalLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.filter_background_gray)
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                toggleSelection(this, option.first)
                updateButtonStates()
            }
        }

        val colorView = createColorView(option.second)
        val textView = createTextView(option.first)

        horizontalLayout.addView(colorView)
        horizontalLayout.addView(textView)

        return horizontalLayout
    }

    private fun createColorView(colorRes: Int): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                setMargins(0, 9, 20, 0)
            }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.filter_background_gray)
            background.setTint(ContextCompat.getColor(requireContext(), colorRes))
        }
    }

    private fun createTextView(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setTypeface(null, android.graphics.Typeface.BOLD)
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun toggleSelection(view: View, option: String) {
        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option)
            view.setBackgroundResource(R.drawable.filter_background_gray)
        } else {
            selectedOptions.add(option)
            view.setBackgroundResource(R.drawable.button_background_pressed)
        }
        filteredColor = selectedOptions.joinToString(", ")
        updateButtonStates()
    }

    private fun updateButtonStates() {
        if (selectedOptions.isNotEmpty()) {
            binding.resetOffButton.setBackgroundResource(R.drawable.reset_on)
            binding.compOffButton.setBackgroundResource(R.drawable.comp_on)
        } else {
            binding.resetOffButton.setBackgroundResource(R.drawable.reset_off)
            binding.compOffButton.setBackgroundResource(R.drawable.comp_off)
        }
    }

    private fun resetSelections(layout: FlexboxLayout) {
        layout.children.forEach { child ->
            child.setBackgroundResource(R.drawable.filter_background_gray)
        }
        selectedOptions.clear() // 선택된 옵션 초기화
        filteredColor = null
        updateButtonStates()
    }

    private fun saveAndFinish() {
        if (filteredColor.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "컬러를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        // 선택된 옵션을 문자열로 변환하여 전달
        val bundle = Bundle().apply {
            putString("category", category)
            putString("ClosetItemFragment", ClosetItemFragment)
            putStringArrayList("selectedOptions", ArrayList(selectedOptions)) // ArrayList로 변환하여 전달
        }

        // 테스트용
        Toast.makeText(requireContext(), "선택된 컬러: $filteredColor", Toast.LENGTH_SHORT).show()
        Toast.makeText(requireContext(), "category: $category", Toast.LENGTH_SHORT).show()

        // ClosetResultFragment로 이동하며 데이터 전달
        findNavController().navigate(R.id.closetResultFragment, bundle)



    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
