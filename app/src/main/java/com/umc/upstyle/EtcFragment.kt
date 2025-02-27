package com.umc.upstyle

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.umc.upstyle.databinding.FragmentEtcBinding

class EtcFragment : Fragment(R.layout.fragment_etc) {

    private var _binding: FragmentEtcBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String? = null
    private var selectedSubCategory: String? = null
    private var selectedFit: String? = null
    private var selectedSize: String? = null
    private var selectedColor: String? = null
    private var selectedEtc: String? = null


    private var kindId: Int? = null
    private var categoryId: Int? = null
    private var fitId: Int? = null
    private var colorId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEtcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 뒤로가기 버튼 클릭 이벤트 설정
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // 이전 액티비티에서 전달된 데이터 수신
        selectedCategory = arguments?.getString("CATEGORY")
        selectedSubCategory = arguments?.getString("SUB_CATEGORY")
        selectedFit = arguments?.getString("FIT")
        selectedSize = arguments?.getString("SIZE")
        selectedColor = arguments?.getString("COLOR")


        kindId = arguments?.getInt("KIND_ID")
        categoryId = arguments?.getInt("CATEGORY_ID")
        fitId = arguments?.getInt("FIT_ID")
        colorId = arguments?.getInt("COLOR_ID")


        // 옷 종류에 대한 설명
        binding.mainTitleTextView.text = "$selectedCategory"


        binding.compButton.setOnClickListener {
            selectedEtc = binding.inputField.text.toString()
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {

        // 데이터를 Bundle에 추가
        val bundle = Bundle().apply {
            putString("CATEGORY", selectedCategory)
            putString("SUB_CATEGORY", selectedSubCategory)
            putString("FIT", selectedFit)
            putString("SIZE", selectedSize)
            putString("COLOR", selectedColor)
            putString("ETC", selectedEtc)

            putInt("CLOTH_ID", 0)
            kindId?.let { putInt("KIND_ID", it) }
            categoryId?.let { putInt("CATEGORY_ID", it) }
            fitId?.let { putInt("FIT_ID", it) }
            colorId?.let { putInt("COLOR_ID", it) }
            putString("ADD_INFO", selectedEtc)
        }

        val action = R.id.todayOotdFragment

        // 네비게이션 액션 수행
        findNavController().navigate(action, bundle)

        Log.d("DEBUG",
            "CATEGORY: $selectedCategory, SUB_CATEGORY: $selectedSubCategory, " +
                    "FIT: $selectedFit, SIZE: $selectedSize, COLOR: $selectedColor, ETC: $selectedEtc")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }
}