package com.umc.upstyle

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LayoutSpace(
    private val spanCount: Int, // 한 줄에 표시할 아이템 개수 (2개)
    private val spacing: Int,   // 아이템 간 간격 (5dp)
    private val edgeSpacing: Int // 화면 좌우 끝 간격 (12.5dp)
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // 아이템의 위치
        val column = position % spanCount // 몇 번째 열인지 계산

        outRect.left = if (column == 0) edgeSpacing else spacing / 2
        outRect.right = if (column == spanCount - 1) edgeSpacing else spacing / 2
        outRect.top = spacing
    }
}
