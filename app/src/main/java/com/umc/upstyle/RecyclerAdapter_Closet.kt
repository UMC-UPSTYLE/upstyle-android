package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter_Closet(private val itemClosetList: List<Item_closet>) :
    RecyclerView.Adapter<RecyclerAdapter_Closet.ViewHolder>() {

    // ViewHolder 클래스
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.item_title) // 제목 TextView
        val image: ImageView = itemView.findViewById(R.id.item_image) // 이미지 ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_closet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemClosetList[position]

        // 텍스트 설정
        holder.title.text = item.description

        // Glide를 사용해 이미지 로드 (URL 또는 로컬 파일 모두 처리 가능)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.image)
    }

    override fun getItemCount(): Int = itemClosetList.size
}
