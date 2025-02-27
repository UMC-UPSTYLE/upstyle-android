package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.ClothPreview

class RecyclerAdapter_Closet(private val itemClosetList: List<ClothPreview>) :
    RecyclerView.Adapter<RecyclerAdapter_Closet.ViewHolder>() {

    // ViewHolder 클래스
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

        // ✅ 텍스트 설정 (kindName이나 categoryName으로 설정)
        val titleText = listOfNotNull(item.categoryName, item.fitName, item.colorName).joinToString(" ")

        holder.title.text = titleText.ifEmpty { "정보 없음" } // 데이터가 없으면 "정보 없음" 표시

        // ✅ 이미지 설정 (ootd.imageUrl이 있으면 표시, 없으면 기본 이미지)
        val imageUrl = item.ootd?.imageUrl

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image)  // 기본 이미지 설정
        }
    }

    override fun getItemCount(): Int = itemClosetList.size
}
