package com.umc.upstyle

import com.umc.upstyle.utils.Item_load
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.umc.upstyle.databinding.FragmentLoadCategoryBinding

class LoadCategoryFragment : Fragment() {
    private var _binding: FragmentLoadCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LoadCategoryFragment에서 데이터 받기
        findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM")?.let { description ->
            findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM_IMAGE_URL")?.let { imageUrl ->
                findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("CATEGORY")?.let { category ->
                    val clothId = arguments?.getInt("CLOTH_ID") ?:0
                    val kindId = arguments?.getInt("KIND_ID") ?:0
                    val categoryId = arguments?.getInt("CATEGORY_ID") ?:0
                    val fitId = arguments?.getInt("FIT_ID") ?:0
                    val colorId = arguments?.getInt("COLOR_ID") ?:0
                    val addInfo = arguments?.getString("ADD_INFO") ?:""

                    // 이제 description과 imageUrl을 사용해서 필요한 작업을 처리
                    val item = Item_load(description, imageUrl,false, clothId, kindId, categoryId, fitId, colorId, addInfo) // 아이템 객체 생성

                // 직전 프래그먼트로 데이터 전달하면서 navigateUp
                findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM", description)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("SELECTED_ITEM_IMAGE_URL", imageUrl)
                findNavController().previousBackStackEntry?.savedStateHandle?.set("CATEGORY", category)


                findNavController().navigateUp()
                }
            }
        }

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach {
            findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(it)
        }


        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OUTER")
            findNavController().navigate(action)
        }

        binding.btnGoTop.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "TOP")
            findNavController().navigate(action)
        }

        binding.btnGoBottom.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BOTTOM")
            findNavController().navigate(action)
        }

        binding.btnGoShoes.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "SHOES")
            findNavController().navigate(action)
        }

        binding.btnGoBag.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BAG")
            findNavController().navigate(action)
        }

        binding.btnGoOther.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OTHER")
            findNavController().navigate(action)
        }


    }




    override fun onDestroyView() {

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach {
            findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(it)
        }
        super.onDestroyView()
        _binding = null // ViewBinding 해제
    }
}
