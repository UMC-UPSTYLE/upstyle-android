package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter_Bookmark(
    private var itemBookmarkList: List<Item_bookmark>
) : RecyclerView.Adapter<RecyclerAdapter_Bookmark.ViewHolder>() {

    // ViewHolder 클래스
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.item_title) // 제목 TextView
        val image: ImageView = itemView.findViewById(R.id.item_image) // 이미지 ImageView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemBookmarkList[position]

        // 텍스트 설정: name이 비어있으면 숨기기 (ALL 버튼 클릭 시)
        if (item.name.isNotEmpty()) {
            holder.title.visibility = View.VISIBLE
            holder.title.text = item.name // 카테고리, 핏, 색깔 순서로 표시
        } else {
            holder.title.visibility = View.GONE // ALL 버튼 클릭 시 텍스트 숨김
        }


        // Glide를 사용해 이미지 로드 (URL 또는 로컬 파일 모두 처리 가능)
        Glide.with(holder.itemView.context)
            .load(item.image)
            .into(holder.image)
    }

    override fun getItemCount(): Int = itemBookmarkList.size

    //리스트 업데이트를 위한 함수
    fun updateList(filteredItems: List<Item_bookmark>) {
        itemBookmarkList = filteredItems // 리스트 변경
        notifyDataSetChanged() // RecyclerView 갱신
    }
}
