package umc.upstyle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.R
import com.umc.upstyle.databinding.ItemLayoutBinding
import com.umc.upstyle.Item

class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // 데이터 바인딩을 사용하여 뷰를 인플레이트합니다.
        val binding = DataBindingUtil.inflate<ItemLayoutBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_layout,
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // 아이템을 뷰에 바인딩합니다.
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder 클래스
    inner class ItemViewHolder(private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            // 데이터 바인딩을 사용하여 아이템을 바인딩
            binding.item = item
            binding.executePendingBindings() // 즉시 바인딩을 적용
        }
    }
}
