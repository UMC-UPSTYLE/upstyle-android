package com.umc.upstyle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter_Search(
    private val itemList: List<Item_search>
) : RecyclerView.Adapter<RecyclerAdapter_Search.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemButton: Button = itemView.findViewById(R.id.item_button) // 아이템 버튼
        val itemTitle: TextView = itemView.findViewById(R.id.item_title) // 아이템 제목
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // 제목 설정
        holder.itemTitle.text = item.description

        // 배경 이미지 설정
        val context = holder.itemButton.context
        val drawable = Glide.with(context)
            .asDrawable()
            .load(item.imageUrl)
            .submit()
            .get() // Glide로 이미지를 동기적으로 로드

        holder.itemButton.background = drawable

        // 클릭 이벤트 설정
        holder.itemButton.setOnClickListener {
            Toast.makeText(context, "클릭한 아이템: ${item.description}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun getItemCount(): Int = itemList.size
}