package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.databinding.ItemLayoutBinding

class RecyclerAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    // ViewHolder 클래스
    class ViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // Glide를 사용해 이미지 로드
        Glide.with(holder.binding.root.context)
            .load(item.imageUrl) // 이미지 URL
            .centerCrop() // 이미지 자르기
            .into(holder.binding.itemImage) // ImageView에 설정

        // 텍스트 바인딩
        holder.binding.item = item // 바인딩된 item 객체에 값을 설정 (item_title의 텍스트가 자동으로 업데이트됨)

    }

    override fun getItemCount(): Int = itemList.size
}
