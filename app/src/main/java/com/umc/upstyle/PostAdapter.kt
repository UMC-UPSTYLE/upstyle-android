import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.R
import com.umc.upstyle.data.model.Post // 이걸로 수정


class PostAdapter(
    private val postList: List<Post>,
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvVoteCount: TextView = view.findViewById(R.id.tvVoteCount)

        fun bind(post: Post) {
            tvTitle.text = post.title
            tvVoteCount.text = "${post.totalResponseCount}명 투표"

            itemView.setOnClickListener {
                onPostClick(post) // 클릭 이벤트 실행
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}