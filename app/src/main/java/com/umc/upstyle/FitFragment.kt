package com.umc.upstyle

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.umc.upstyle.databinding.ActivityFitBinding

class FitFragment : Fragment(R.layout.activity_fit) {

    private var _binding: ActivityFitBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String? = null
    private var selectedSubCategory: String? = null
    private var selectedFit: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivityFitBinding.bind(view)  // 바인딩 초기화

        val goBackButton = binding.backButton
        goBackButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 이전 액티비티에서 전달된 데이터 수신
        selectedCategory = arguments?.getString("CATEGORY")
        selectedSubCategory = arguments?.getString("SUB_CATEGORY")

        // 옷 종류에 대한 설명
        binding.mainTitleTextView.text = selectedCategory

        // Title 설정
        binding.titleTextView.text = "핏/사이즈를 선택해주세요!"

        // 핏 선택 옵션 설정
        setupFitOptions()

        // 다음 버튼 클릭 시 처리
        binding.nextButton.setOnClickListener {
            navigateToNextStep()
        }
    }

    private fun setupFitOptions() {
        val options = listOf("슬림핏", "레귤러핏", "오버핏")
        createButtons(binding.optionsLayout, options) { selectedOption ->
            selectedFit = selectedOption
            binding.nextButton.setBackgroundResource(R.drawable.next_on) // 버튼 활성화 상태로 변경
        }
    }

    private fun createButtons(layout: FlexboxLayout, options: List<String>, onClick: (String) -> Unit) {
        layout.removeAllViews()

        options.forEach { option ->
            val button = Button(requireContext()).apply {
                text = option
                setTypeface(null, android.graphics.Typeface.BOLD)
                background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background)
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setPadding(40, 10, 40, 10)

                setOnClickListener {
                    // 다른 버튼의 배경을 초기화
                    layout.children.forEach { (it as Button).background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.button_background)
                    }
                    // 선택된 버튼 배경 변경
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_pressed)
                    onClick(option)
                }
            }

            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                100
            ).apply {
                setMargins(16, 16, 16, 16)
            }

            layout.addView(button, layoutParams)
        }
    }

    private fun navigateToNextStep() {
        if (selectedFit.isNullOrEmpty()) {
            // 선택하지 않았을 경우 사용자에게 알림
            Toast.makeText(requireContext(), "핏을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 다음 Fragment로 이동
        val fragment = ColorFragment().apply {
            arguments = Bundle().apply {
                putString("CATEGORY", selectedCategory)
                putString("SUB_CATEGORY", selectedSubCategory)
                putString("FIT", selectedFit)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)  // fragment_container는 해당 Fragment를 포함할 컨테이너의 ID
            .addToBackStack(null)  // 백 스택에 추가하여 뒤로가기를 사용할 수 있게 함
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}
