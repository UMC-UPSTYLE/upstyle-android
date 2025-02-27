package com.umc.upstyle

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.Window
import com.umc.upstyle.R

class LoadItemPopupDialog(
    context: Context,
    private val onCreateNewClicked: () -> Unit,  // "새로 생성" 버튼 클릭 시 동작
    private val onLoadPreviousClicked: () -> Unit // "불러오기" 버튼 클릭 시 동작
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 타이틀 바 제거
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 팝업 레이아웃 설정
        setContentView(R.layout.load_item_popup)

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // 메시지 텍스트 설정 (필요 시 동적으로 변경 가능)
        val tvPopupMessage: TextView = findViewById(R.id.tv_popup_message)
        tvPopupMessage.text = "지난 착장의 제품이름을 불러올 수 있어요."

        // 버튼 설정
        val btnCreateNew: Button = findViewById(R.id.btn_create_new)
        val btnLoadPrevious: Button = findViewById(R.id.btn_load_previous)

        // "새로 생성" 버튼 클릭 리스너
        btnCreateNew.setOnClickListener {
            onCreateNewClicked.invoke()
            dismiss() // 다이얼로그 닫기
        }

        // "불러오기" 버튼 클릭 리스너
        btnLoadPrevious.setOnClickListener {
            onLoadPreviousClicked.invoke()
            dismiss() // 다이얼로그 닫기
        }
    }
}
