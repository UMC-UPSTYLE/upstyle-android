package com.umc.upstyle

import com.umc.upstyle.utils.Item_load
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecyclerAdapter_Load(
    private var itemList: MutableList<Item_load>,
    private val onItemClick: (Item_load, Any?) -> Unit
) : RecyclerView.Adapter<RecyclerAdapter_Load.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: AppCompatImageButton = itemView.findViewById(R.id.item_image)
        val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        val icUnselected: ImageButton = itemView.findViewById(R.id.icunselected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_load, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val context = holder.imageButton.context

        holder.itemTitle.text = item.description

        Glide.with(context)
            .load(item.imageUrl)
            .into(holder.imageButton)

        if (position == selectedPosition) {
            holder.icUnselected.setImageResource(R.drawable.ic_selected)
        } else {
            holder.icUnselected.setImageResource(R.drawable.ic_unselected)
        }

        holder.imageButton.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = position

            onItemClick(item, position)

            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun setSelectedPosition(position: Any?) {
        val previousSelected = selectedPosition
        selectedPosition = position as Int

        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }

    fun updateData(newItems: MutableList<Item_load>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }



}

