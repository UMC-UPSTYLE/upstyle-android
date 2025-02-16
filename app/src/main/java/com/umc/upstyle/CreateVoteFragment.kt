package com.umc.upstyle

import com.umc.upstyle.utils.Item_load
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umc.upstyle.adapter.VoteItemAdapter
import com.umc.upstyle.data.viewmodel.PostViewModel
import com.umc.upstyle.databinding.FragmentCreateVoteBinding
import com.umc.upstyle.model.VoteItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateVoteFragment : Fragment() {
    private var _binding: FragmentCreateVoteBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null // ✅ lateinit 제거 및 nullable 변경

    private lateinit var viewModel: PostViewModel
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private var voteItemList = mutableListOf<VoteItem>()
    private lateinit var voteItemAdapter: VoteItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVoteBinding.inflate(inflater, container, false)
        editTextTitle = binding.etTitle
        editTextContent = binding.etContent

        // ViewModel 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)

        // ViewModel에 저장된 데이터가 있으면 복원
        editTextTitle.setText(viewModel.postTitle)
        editTextContent.setText(viewModel.postContent)

        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM")?.let { description ->
            findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM_IMAGE_URL")
                ?.let { imageUrl ->
                    findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("CATEGORY")
                        ?.let { category ->
                            val clothId = arguments?.getInt("CLOTH_ID") ?:0
                            val kindId = arguments?.getInt("KIND_ID") ?:0
                            val categoryId = arguments?.getInt("CATEGORY_ID") ?:0
                            val fitId = arguments?.getInt("FIT_ID") ?:0
                            val colorId = arguments?.getInt("COLOR_ID") ?:0
                            val addInfo = arguments?.getString("ADD_INFO") ?:""

                            // 이제 description과 imageUrl을 사용해서 필요한 작업을 처리
                            val item = Item_load(description, imageUrl,false, clothId, kindId, categoryId, fitId, colorId, addInfo) // 아이템 객체 생성

                            // 이미지 로드 처리

                            binding.imageContainer.visibility = View.VISIBLE
                            binding.imgSelected.visibility = View.VISIBLE
                            binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
                            binding.btnImageUpload.visibility = View.INVISIBLE

                            Glide.with(requireContext())
                                .load(item.imageUrl)
                                .into(binding.imgSelected)

                        }
                }
        }

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach {
            findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(it)
        }

        // 사진 등록 버튼 이벤트
        binding.btnImageUpload.setOnClickListener { showPhotoOptions() }

        // 사진 삭제 버튼 이벤트
        binding.btnRemoveImage.setOnClickListener {
            // 사진 초기화
            binding.imgSelected.setImageURI(null)  // 이미지 초기화
            binding.imgSelected.visibility = View.GONE  // 이미지 숨기기
            binding.btnRemoveImage.visibility = View.GONE  // X 버튼 숨기기
            binding.imageContainer.visibility = View.INVISIBLE  // imageContainer 숨기기
            binding.btnImageUpload.visibility = View.VISIBLE  // 사진 업로드 버튼 보이기

            // 사진 URI도 초기화
            photoUri = null
        }
    }


    private fun setupRecyclerView() {
        if (voteItemList.isEmpty()) {
            voteItemList.add(VoteItem(id = 1, imageUrl = "", name = "항목 입력"))
        }

        voteItemAdapter = VoteItemAdapter(
            voteItems = voteItemList,
            onItemClick = { showPhotoOptions() },
            onAddClick = {
                addNewVoteItem()
                setRecyclerViewHeightBasedOnItems(binding.voteItemRecyclerView)
            }
        )

        binding.voteItemRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = voteItemAdapter
            isNestedScrollingEnabled = false // 리사이클러뷰의 스크롤을 비활성화
        }
    }

    private fun addNewVoteItem() {
        if (voteItemList.size >= 4) {
            return  // 4개 이상이면 추가 안 함
        }
        voteItemList.add(VoteItem(id = voteItemList.size + 1, imageUrl = "", name = "항목 입력"))
        voteItemAdapter.notifyItemInserted(voteItemList.size - 1)

        // 만약 4개가 되어서 추가 버튼이 사라져야 한다면 마지막 아이템 삭제
        if (voteItemList.size == 4) {
            voteItemAdapter.notifyItemRemoved(voteItemList.size)
        }
    }


    // ✅ TakePictureLauncher - photoUri가 null이 아니면만 처리
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.imageContainer.visibility = View.VISIBLE
            binding.imgSelected.visibility = View.VISIBLE
            binding.imgSelected.setImageURI(photoUri) // 촬영한 사진 표시
            binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
            binding.btnImageUpload.visibility = View.INVISIBLE
            Toast.makeText(requireContext(), "사진 촬영 성공!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "사진 촬영 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ 이미지 선택
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(it)
            if (savedPath != null) {
                binding.imageContainer.visibility = View.VISIBLE
                binding.imgSelected.visibility = View.VISIBLE
                binding.imgSelected.setImageURI(it) // ✅ 직접 선택한 URI 사용
                binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
                binding.btnImageUpload.visibility = View.INVISIBLE
                saveImagePath(savedPath)
            } else {
                Toast.makeText(requireContext(), "이미지 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoOptions() {
        val votePopup = VotePopupDialog(
            onTakePhoto = { takePhoto() },
            onChoosePhoto = { selectImageFromGallery() },
            onLoadItem = { findNavController().navigate(R.id.loadCategoryFragment) },
            onCancel = { /* 취소 버튼 동작 */ }
        )
        votePopup.show(parentFragmentManager, "VotePopupDialog")
    }

//    private fun showPhotoOptionsItem(position: Int) {
//        val votePopup = VotePopupDialog(
//            onTakePhoto = { takePhotoForItem(position) },
//            onChoosePhoto = { selectImageForItem(position) },
//            onLoadItem = { findNavController().navigate(R.id.loadCategoryFragment) },
//            onCancel = { /* 취소 버튼 동작 */ }
//        )
//        votePopup.show(parentFragmentManager, "VotePopupDialog")
//    }
//
//    private fun takePhotoForItem(position: Int) {
//        try {
//            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val photoFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
//
//            val uri = FileProvider.getUriForFile(
//                requireContext(),
//                "${requireContext().packageName}.fileprovider",
//                photoFile
//            )
//
//            photoUri = uri
//            takePictureForItemLauncher.launch(uri to position)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "사진 촬영 준비 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun selectImageForItem(position: Int) {
//        pickImageForItemLauncher.launch(position)
//    }





    private fun takePhoto() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)

            // ✅ photoUri를 null 체크 후 초기화
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "사진 촬영 준비 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun selectImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "selected_image_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = file.outputStream()

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImagePath(path: String) {
        val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)
        preferences.edit().putString("SAVED_IMAGE_PATH", path).apply()
    }

    fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        val layoutManager = recyclerView.layoutManager ?: return

        recyclerView.post {
            var totalHeight = 0
            for (i in 0 until adapter.itemCount) {
                val viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
                adapter.onBindViewHolder(viewHolder, i)

                // View의 측정을 완료한 후 높이를 얻기
                viewHolder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
                )

                totalHeight += viewHolder.itemView.measuredHeight
            }

            // 최대 높이를 설정하여 리사이클러뷰가 무한히 늘어나는 것을 방지
            val MAX_HEIGHT = 1300 // 예시로 최대 높이 설정
            val layoutParams = recyclerView.layoutParams
            layoutParams.height = Math.min(totalHeight, MAX_HEIGHT)
            recyclerView.layoutParams = layoutParams

//            val layoutParams = recyclerView.layoutParams
//            layoutParams.height = totalHeight
//            recyclerView.layoutParams = layoutParams
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
