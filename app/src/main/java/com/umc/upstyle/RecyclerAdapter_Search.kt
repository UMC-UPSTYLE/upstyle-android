import Item_search
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.R
import com.umc.upstyle.SearchResultFragment

class RecyclerAdapter_Search(
    private val itemList: List<Item_search>,
    private val itemClickListener: (Item_search) -> Unit
) : RecyclerView.Adapter<RecyclerAdapter_Search.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemButton: AppCompatImageButton = itemView.findViewById(R.id.item_image) // 아이템 버튼
        val itemTitle: TextView = itemView.findViewById(R.id.item_title) // 아이템 제목
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        val context = holder.itemButton.context

        // 제목 설정
        holder.itemTitle.text = item.description

        Log.d("RecyclerAdapter_Search", "Loading image URL: ${item.imageUrl}")

        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.bg_sub_color_1) // ✅ 로딩 중일 때 사용할 기본 이미지
            .error(R.drawable.bg_gray_15) // ✅ 로딩 실패 시 대체 이미지
            .into(holder.itemButton) // ✅ `background` 대신 `setImageDrawable()` 사용됨


        // 클릭 이벤트 설정: itemClickListener 호출
        holder.itemButton.setOnClickListener {
            itemClickListener(item) // 아이템 클릭 리스너 호출
        }
    }
 override fun getItemCount(): Int = itemList.size


}
