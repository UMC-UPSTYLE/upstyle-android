package com.umc.upstyle

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.umc.upstyle.databinding.FragmentSearchItemBinding
import java.io.File

class SearchItemFragment : Fragment(R.layout.fragment_search_item) {

    private var _binding: FragmentSearchItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        val items = loadItemsFromPreferences()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = SearchItemAdapter(items) { item ->
            Toast.makeText(requireContext(), "클릭한 아이템: ${item.description}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadItemsFromPreferences(): List<Item_search> {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)

        // 저장된 데이터 로드
        val description = preferences.getString("DESCRIPTION", "설명 없음")
        val savedImagePath = preferences.getString("SAVED_IMAGE_PATH", null)

        // 기본 샘플 데이터
        val items = mutableListOf(
            Item_search("샘플 1", "https://example.com/image1.jpg"),
            Item_search("샘플 2", "https://example.com/image2.jpg")
        )

        // 저장된 데이터 추가
        if (!savedImagePath.isNullOrEmpty() && !description.isNullOrEmpty()) {
            val file = File(savedImagePath)
            if (file.exists()) {
                val fileUri = Uri.fromFile(file).toString()
                items.add(0, Item_search(description, fileUri))
            }
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // RecyclerView Adapter
    class SearchItemAdapter(
        private val items: List<Item_search>,
        private val onItemClick: (Item_search) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val imageView: android.widget.ImageView = view.findViewById(R.id.item_image)
            val textView: android.widget.TextView = view.findViewById(R.id.item_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_search, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textView.text = item.description

            // Glide를 사용하여 이미지 로드
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                //.placeholder(R.drawable.placeholder_image)
                //.error(R.drawable.error_image)
                .into(holder.imageView)

            // 아이템 클릭 이벤트
            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}