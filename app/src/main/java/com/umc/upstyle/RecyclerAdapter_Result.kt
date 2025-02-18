package com.umc.upstyle

import Item_result
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.R
import com.umc.upstyle.SearchResultFragment

class RecyclerAdapter_Result(
    private val items: List<Item_result>,
    private val onItemClick: (Item_result) -> Unit
) : RecyclerView.Adapter<RecyclerAdapter_Result.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val titleView: TextView = itemView.findViewById(R.id.item_title)

        fun bind(item: Item_result) {
            Log.d("ADAPTER_BIND", "아이템 바인딩: ${item.description} (${item.imageUrl})") // ✅ 로그 추가
            titleView.text = item.description

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(imageView)

            // ✅ 이미지 클릭 시 이동
            imageView.setOnClickListener {
                Log.d("RecyclerView", "Image clicked: ${item.description}")
                onItemClick(item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        Log.d("ADAPTER_ITEM_COUNT", "아이템 개수: ${items.size}") // ✅ 로그 추가
        return items.size
    }


}