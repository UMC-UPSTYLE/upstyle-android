import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.umc.upstyle.R
import com.umc.upstyle.data.model.Request

class RequestAdapter(
    private val requestList: List<Request>,
    private val onRequestClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvCount: TextView = view.findViewById(R.id.tv_count)
        private val btnBox: View = view.findViewById(R.id.btn_box)


        fun bind(request: Request) {
            tvTitle.text = request.title
            tvCount.text = "${request.commentCount}"

            btnBox.setOnClickListener { // 네모 상자를 눌러야 이벤트 발생
                onRequestClick(request)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requestList[position])
    }

    override fun getItemCount(): Int = requestList.size
}
