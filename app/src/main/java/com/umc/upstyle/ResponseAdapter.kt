package com.umc.upstyle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.data.model.CodiResPreview

class ResponseAdapter(private val comments: List<CodiResPreview>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_response, parent, false)
        return ResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size

    inner class ResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvResNum: TextView = itemView.findViewById(R.id.tvResNum)

        fun bind(comment: CodiResPreview) {
            tvUsername.text = comment.user.nickname
            tvResNum.text = "응답 ${position + 1}"

            itemView.setOnClickListener {
                onItemClick(comment.id) // 클릭된 아이템의 id를 전달
            }
        }
    }
}
