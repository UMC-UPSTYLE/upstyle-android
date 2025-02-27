package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.VoteOption

class VoteOptionAdapter(
    private var items: List<VoteOption>,
    private val onItemSelected: (VoteOption) -> Unit,
    private var isVoteCompleted: Boolean = false // 투표 완료 상태를 추적
) : RecyclerView.Adapter<VoteOptionAdapter.ViewHolder>() {

    private var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vote_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedPosition)

        if (!isVoteCompleted) {
            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition

                if (previousPosition != null) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition!!)

                onItemSelected(item)
            }
        } else {
            holder.itemView.isClickable = false // 투표 완료 시 클릭 비활성화
            holder.itemView.isFocusable = false
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<VoteOption>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_vote_item_image)
        private val nameText: TextView = itemView.findViewById(R.id.tv_option_text)
        private val checkMark: ImageView = itemView.findViewById(R.id.iv_selected)

        fun bind(item: VoteOption, isSelected: Boolean) {
            Glide.with(itemView.context).load(item.imageUrl).into(imageView)
            nameText.text = item.name
            checkMark.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }

    fun setVoteCompleted(isCompleted: Boolean) {
        isVoteCompleted = isCompleted
        notifyDataSetChanged() // 변경 사항을 반영하기 위해 adapter 업데이트
    }
}
