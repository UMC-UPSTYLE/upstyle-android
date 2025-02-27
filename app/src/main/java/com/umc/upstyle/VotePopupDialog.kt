package com.umc.upstyle

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.umc.upstyle.databinding.VotePopupBinding

class VotePopupDialog(
    private val onTakePhoto: () -> Unit,
    private val onChoosePhoto: () -> Unit,
    private val onLoadItem: () -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    private var _binding: VotePopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VotePopupBinding.inflate(inflater, container, false)

        dialog?.window?.apply {
            setGravity(Gravity.BOTTOM) // 다이얼로그를 하단으로 정렬
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) // 가로 전체, 높이는 내용에 맞춤
            attributes.y = 80
        }
        // 배경을 투명하게 설정
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "사진찍기" 버튼
        binding.takePhoto.setOnClickListener {
            onTakePhoto()
            dismiss() // 팝업 닫기
        }

        // "사진 보관함" 버튼
        binding.choosePhoto.setOnClickListener {
            onChoosePhoto()
            dismiss() // 팝업 닫기
        }

        // "제품 불러오기" 버튼
        binding.loadItem.setOnClickListener {
            onLoadItem()
            dismiss() // 팝업 닫기
        }

        // "취소" 버튼
        binding.cancel.setOnClickListener {
            onCancel()
            dismiss() // 팝업 닫기
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
