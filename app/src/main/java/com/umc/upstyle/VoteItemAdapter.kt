package com.umc.upstyle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.databinding.ItemVoteBinding
import com.umc.upstyle.data.model.VoteItem

class VoteItemAdapter(
    private val voteItems: MutableList<VoteItem>,
    private val onItemClick: (Int) -> Unit,
    private val onAddClick: () -> Unit,
    private val onTextChange: (Int, String) -> Unit // Callback to handle text changes
) : RecyclerView.Adapter<VoteItemAdapter.VoteItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteItemViewHolder {
        val binding = ItemVoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoteItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoteItemViewHolder, position: Int) {
        if (position < voteItems.size) {
            holder.bind(voteItems[position])  // 기존 아이템 바인딩
        } else {
            holder.bindAddButton()  // 추가 버튼 바인딩
        }
    }

    override fun getItemCount(): Int {
        return if (voteItems.size >= 4) voteItems.size else voteItems.size + 1
    }

    // 아이템의 이미지 URL을 업데이트하는 메서드
    fun updateImageAtPosition(position: Int, newImageUrl: String, clothId: Int = 0) {
        try {
            if (position in voteItems.indices) {
                voteItems[position].imageUrl = newImageUrl
                voteItems[position].clothId = clothId
                notifyItemChanged(position) // 해당 아이템만 갱신
                Log.d("VoteItemAdapter", "position: $position 갱신 완료 ${voteItems[position].imageUrl} 입니다")
            } else {
                Log.e("VoteItemAdapter", "Invalid position: $position, voteItems size: ${voteItems.size}")
            }
        } catch (e: Exception) {
            Log.e("VoteItemAdapter", "Error updating image at position $position: ${e.message}", e)
        }
    }


    inner class VoteItemViewHolder(private val binding: ItemVoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(voteItem: VoteItem) {
            if (voteItem.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(voteItem.imageUrl)  // viewModel에서 가져온 이미지 URL
                    .into(binding.ivVoteItemImage)  // 이미지 뷰에 로드

                binding.icPicture.visibility = View.GONE
            }

            binding.etVoteItemText.setText(voteItem.name) // EditText 값 설정

            // Add text change listener to update the value in the ViewModel or List
            binding.etVoteItemText.addTextChangedListener {
                onTextChange(adapterPosition, it.toString())  // Notify when text changes
            }

            binding.root.setOnClickListener { onItemClick(adapterPosition) }
        }

        fun bindAddButton() {
            if (voteItems.size >= 4) {
                binding.root.visibility = View.GONE  // 추가 버튼 숨기기
            } else {
                binding.root.visibility = View.VISIBLE
                binding.icPicture.setImageResource(com.umc.upstyle.R.drawable.ic_plus)
                binding.etVoteItemText.alpha = 0.3f
                binding.barFirst.alpha = 0.3f
                binding.barSecond.alpha = 0.3f
                binding.ivVoteItemImage.alpha = 0.3f
                binding.ivPencil.visibility = View.GONE
                binding.root.setOnClickListener { onAddClick() }
            }
        }
    }
}

