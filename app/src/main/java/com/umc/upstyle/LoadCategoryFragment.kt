package com.umc.upstyle

import com.umc.upstyle.utils.Item_load
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.umc.upstyle.data.viewmodel.CategoryViewModel
import com.umc.upstyle.databinding.FragmentLoadCategoryBinding

class LoadCategoryFragment : Fragment() {
    private var _binding: FragmentLoadCategoryBinding? = null
    private val binding get() = _binding!!
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val args: LoadCategoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val position = args.position // 전달받은 position 값
        val username = args.username // 전달받은 username
//        Toast.makeText(context, "받아온 포지션: $position", Toast.LENGTH_SHORT).show()
        categoryViewModel.position = position
        categoryViewModel.username = username
        _binding = FragmentLoadCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LoadCategoryFragment에서 데이터 받기
        findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM")?.let { description ->
            findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM_IMAGE_URL")?.let { imageUrl ->
                findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("CATEGORY")?.let { category ->
                    findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("CLOTH_ID")?.let { clothId ->

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
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("CLOTH_ID", clothId)
//                    Toast.makeText(context, "로드카테고리에서 전송 포지션: ${categoryViewModel.position}", Toast.LENGTH_SHORT).show()
                    findNavController().previousBackStackEntry?.savedStateHandle?.set("POSITION", categoryViewModel.position)

                    findNavController().navigateUp()
                    }
                }
            }
        }

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach { key ->
            val value = findNavController().previousBackStackEntry?.savedStateHandle?.get<Any>(key)
            when (value) {
                is String -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(key)
                is Int -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Int>(key)
                is Boolean -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Boolean>(key)
                is Float -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Float>(key)
                is Double -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Double>(key)
                else -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Any>(key)
            }
        }

        binding.tvUsername.text = categoryViewModel.username


        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        binding.btnGoOuter.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OUTER", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoTop.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "TOP", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoBottom.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BOTTOM", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoShoes.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "SHOES", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoBag.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "BAG", userId = -1)
            findNavController().navigate(action)
        }

        binding.btnGoOther.setOnClickListener {
            val action = LoadCategoryFragmentDirections.actionLoadCategoryFragmentToLoadItemFragment(category = "OTHER", userId = -1)
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
