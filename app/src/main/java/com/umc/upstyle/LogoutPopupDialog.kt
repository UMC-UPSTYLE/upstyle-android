package com.umc.upstyle

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.databinding.DataBindingUtil.setContentView

class LogoutPopupDialog (
    context: Context,
    private val onNoClicked: () -> Unit,  // "새로 생성" 버튼 클릭 시 동작
    private val onYesClicked: () -> Unit // "불러오기" 버튼 클릭 시 동작
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 타이틀 바 제거
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 팝업 레이아웃 설정
        setContentView(R.layout.logout_popup)

        // 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // 버튼 설정
        val btnNo: Button = findViewById(R.id.btn_no)
        val btnYes: Button = findViewById(R.id.btn_yes)

        // "아니요" 버튼 클릭 리스너
        btnNo.setOnClickListener {
            onNoClicked.invoke()
            dismiss() // 다이얼로그 닫기
        }

        // "네" 버튼 클릭 리스너
        btnYes.setOnClickListener {
            onYesClicked.invoke()
            dismiss() // 다이얼로그 닫기
        }
    }
}
