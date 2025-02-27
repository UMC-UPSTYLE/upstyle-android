// VotePreviewAdapter.kt
package com.umc.upstyle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.data.model.VotePreview
import com.umc.upstyle.databinding.RecyclerItemVoteBinding

class RecyclerAdapter_VotePreview(
    private val voteList: List<VotePreview>,
    private val onItemClick: (VotePreview) -> Unit
) : RecyclerView.Adapter<RecyclerAdapter_VotePreview.VoteViewHolder>() {

    inner class VoteViewHolder(private val binding: RecyclerItemVoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vote: VotePreview) {
            binding.voteTitle.text = vote.title
            binding.responseCount.text = "응답 수: ${vote.totalResponseCount}"

            binding.root.setOnClickListener {
                onItemClick(vote)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val binding = RecyclerItemVoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(voteList[position])
    }

    override fun getItemCount(): Int = voteList.size
}
