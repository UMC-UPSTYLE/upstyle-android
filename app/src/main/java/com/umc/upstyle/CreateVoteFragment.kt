package com.umc.upstyle

import android.content.Context
import com.umc.upstyle.utils.Item_load
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.umc.upstyle.VoteItemAdapter
import com.umc.upstyle.data.model.AccountInfoDTO
import com.umc.upstyle.data.model.ApiResponse
import com.umc.upstyle.data.viewmodel.PostViewModel
import com.umc.upstyle.databinding.FragmentCreateVoteBinding
import com.umc.upstyle.data.model.VoteItem
import com.umc.upstyle.data.model.VoteRequest
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.RequestService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.network.UserApiService
import com.umc.upstyle.data.network.VoteService
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateVoteFragment : Fragment() {
    private var _binding: FragmentCreateVoteBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null // ✅ lateinit 제거 및 nullable 변경

    private var currentPosition: Int = -1  // position 값을 저장할 변수

    private lateinit var viewModel: PostViewModel
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private var voteItemList = mutableListOf<VoteItem>()
    private lateinit var voteItemAdapter: VoteItemAdapter
    private val userApiService = RetrofitClient.createService(UserApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVoteBinding.inflate(inflater, container, false)
        editTextTitle = binding.etTitle
        editTextContent = binding.etContent

        val sharedPref = requireContext().getSharedPreferences("Auth", Context.MODE_PRIVATE)
        val jwtToken = sharedPref.getString("jwt_token", null)


        userApiService.getUserInfo("Bearer $jwtToken").enqueue(object :
            Callback<ApiResponse<AccountInfoDTO>> {
            override fun onResponse(
                call: Call<ApiResponse<AccountInfoDTO>>,
                response: Response<ApiResponse<AccountInfoDTO>>
            ) {
                if (response.isSuccessful) {
                    viewModel.username = response.body()?.result?.nickname.toString()

                } else {
                }

            }

            override fun onFailure(call: Call<ApiResponse<AccountInfoDTO>>, t: Throwable) {
                Log.e("Account", "사용자 정보 요청 실패: ${t.message}")
            }
        })

        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(PostViewModel::class.java)

        // ViewModel에 저장된 데이터가 있으면 복원
        editTextTitle.setText(viewModel.postTitle)
        editTextContent.setText(viewModel.postContent)

        val imageUrl = viewModel.imageUrl
        // Glide로 이미지를 로드
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)  // viewModel에서 가져온 이미지 URL
                .into(binding.imgSelected)  // 이미지 뷰에 로드

            binding.imageContainer.visibility = View.VISIBLE
            binding.imgSelected.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
            binding.btnImageUpload.visibility = View.INVISIBLE
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp() // 이전 Fragment로 이동
        }

        // 뒤로 가기 버튼 클릭 시 navigateUp() 실행
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM")?.let { description ->
            findNavController().currentBackStackEntry?.savedStateHandle?.get<String>("SELECTED_ITEM_IMAGE_URL")
                ?.let { imageUrl ->
                    findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("POSITION")
                        ?.let { position ->
                            findNavController().currentBackStackEntry?.savedStateHandle?.get<Int>("CLOTH_ID")
                                ?.let { clothId ->
//                                    Toast.makeText(
//                                        context,
//                                        "받아온 clothId: $clothId",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                    // 이미지 로드 처리
                                    if (position == -1) {
                                        // 그냥 사진 등록
                                        binding.imageContainer.visibility = View.VISIBLE
                                        binding.imgSelected.visibility = View.VISIBLE
                                        binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
                                        binding.btnImageUpload.visibility = View.INVISIBLE

                                        viewModel.imageUrl = imageUrl

                                        Glide.with(requireContext())
                                            .load(viewModel.imageUrl)
                                            .into(binding.imgSelected)

                                        viewModel.imageUrl = imageUrl


                                    } else if (position in 0..3) { // position이 0, 1, 2, 3일 경우, 해당하는 아이템의 ImageView에 이미지 세팅
                                        // voteItemList에서 해당하는 position의 아이템 가져오기
                                        val selectedItem = voteItemList.getOrNull(position)

                                        // 해당 아이템이 존재하면, 그 아이템의 이미지 URL 업데이트
                                        selectedItem?.let { item ->
                                            Log.d("check", "${position}의 아이템 존재")
                                            // 어댑터의 updateImageAtPosition 호출하여 해당 아이템의 이미지 URL을 업데이트
                                            voteItemAdapter.updateImageAtPosition(
                                                position,
                                                imageUrl,
                                                clothId
                                            )
//                                            Toast.makeText(
//                                                context,
//                                                "업데이트된 이미지 clothId${voteItemList[position].clothId}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
                                        }
                                    } else {
                                    }
                                }

                        }
                }
        }

        // 날려버리기
        findNavController().previousBackStackEntry?.savedStateHandle?.keys()?.forEach { key ->
            val value = findNavController().previousBackStackEntry?.savedStateHandle?.get<Any>(key)
            when (value) {
                is String -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<String>(key)
                is Int -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Int>(key)
                is Boolean -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Boolean>(key)
                is Float -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Float>(key)
                is Double -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Double>(key)
                else -> findNavController().previousBackStackEntry?.savedStateHandle?.remove<Any>(key)
            }
        }

        // 사진 등록 버튼 이벤트
        binding.btnImageUpload.setOnClickListener { showPhotoOptions(-1) }

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
            viewModel.imageUrl = ""
        }

        binding.btnUpload.setOnClickListener{
            lifecycleScope.launch { sendToServerWithFirebaseUpload() }
        }
    }

    private suspend fun uploadImageWithOverlay(uri: Uri): String? {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "images/${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(fileName)

        binding.overlayProgress.visibility = View.VISIBLE

        return suspendCoroutine { continuation ->
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                if (isAdded && view != null) {
                    val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    val progressBar = binding.overlayProgress.findViewById<ProgressBar>(R.id.overlayProgressBar)
                    progressBar?.progress = progress
                    binding.tvOverlayProgress.text = "${progress}%"
                }
            }

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    binding.overlayProgress.visibility = View.GONE
                    continuation.resume(downloadUri.toString())
                }.addOnFailureListener { continuation.resume(null) }
            }.addOnFailureListener {
                binding.overlayProgress.visibility = View.GONE
                continuation.resume(null)
            }
        }
    }

    private suspend fun updateVoteItemImageUrls() {
        // ViewModel에서 nameList 가져오기
        val voteItemList = viewModel.nameList.value

        // 각 voteItem의 이미지 URL을 확인하고, http로 시작하지 않으면 Firebase에 업로드
        voteItemList?.forEachIndexed { position, voteItem ->
            if (voteItem.imageUrl.isNotEmpty() && !voteItem.imageUrl.startsWith("http")) {
                // Firebase에 이미지 업로드
                val newImageUrl = uploadImageWithOverlay(Uri.parse(voteItem.imageUrl))
                if (newImageUrl != null) {
                    // 이미지 URL 업데이트
                    viewModel.updateImageAtPosition(position, newImageUrl)
                } else {
                    Log.e("CreateVoteFragment", "Firebase 이미지 업로드 실패")
                }
            }
        }

        // viewModel.imageUrl 확인하고, http로 시작하지 않으면 Firebase에 업로드
        if (viewModel.imageUrl.isNotEmpty() && !viewModel.imageUrl.startsWith("http")) {
            // Firebase에 이미지 업로드
            val newImageUrl = uploadImageWithOverlay(Uri.parse(viewModel.imageUrl))
            if (newImageUrl != null) {
                // viewModel의 imageUrl 업데이트
                viewModel.imageUrl = newImageUrl
                Log.d("CreateVoteFragment", "viewModel.imageUrl 업데이트 완료: $newImageUrl")
            } else {
                Log.e("CreateVoteFragment", "Firebase 이미지 업로드 실패: viewModel.imageUrl")
            }
        }
    }

    private suspend fun sendToServerWithFirebaseUpload() {
        // nameList에서 각 VoteItem에 대해 이미지 URL이 http로 시작하지 않으면 Firebase에 업로드
        updateVoteItemImageUrls()

        // 서버로 요청 보낼 준비
        val voteItems = viewModel.nameList.value ?: return
        val voteRequest= VoteRequest(
            userId = 1,
            title = binding.etTitle.text.toString(),
            body = binding.etContent.text.toString(),
            imageUrl = viewModel.imageUrl,
            optionList = voteItems
        )

        Log.d("CreateVoteFragment", "전달한 imageUrl: ${voteRequest.imageUrl}")

        val voteService = RetrofitClient.createService(VoteService::class.java)
        try {
            // 서버 요청 보내기
            val response = voteService.createVote(voteRequest)
            if (response.isSuccessful) {
                findNavController().navigate(R.id.chatFragment)
                Log.d("CreateVoteFragment", "업로드 성공")
            } else {
                Log.e("CreateVoteFragment", "업로드 실패: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("CreateVoteFragment", "서버 요청 중 예외 발생: ${e.message}", e)
        }
    }


    private fun setupRecyclerView() {
        if (voteItemList.isEmpty()) {
            voteItemList.add(VoteItem(clothId = 0, imageUrl = "", name = "항목 입력"))
        }

        voteItemAdapter = VoteItemAdapter(
            voteItems = voteItemList,
            onItemClick = { position -> showPhotoOptions(position) },
            onAddClick = {
                addNewVoteItem()
                setRecyclerViewHeightBasedOnItems(binding.voteItemRecyclerView)
            },
            onTextChange = { position, text ->
                voteItemList[position].name = text // Update the voteItem name
                viewModel.nameList.value = voteItemList // Update ViewModel with the new list
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
        voteItemList.add(VoteItem(clothId = 0, imageUrl = "", name = "항목 입력"))
        voteItemAdapter.notifyItemInserted(voteItemList.size - 1)

        // 만약 4개가 되어서 추가 버튼이 사라져야 한다면 마지막 아이템 삭제
        if (voteItemList.size == 4) {
            voteItemAdapter.notifyItemRemoved(voteItemList.size)
        }
    }


    // ✅ TakePictureLauncher - photoUri가 null이 아니면만 처리
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            if (currentPosition == -1) {
                // position이 -1이면 binding.imgSelected에 사진 로드
                binding.imgSelected.setImageURI(photoUri)
                viewModel.imageUrl = photoUri.toString()
                binding.imageContainer.visibility = View.VISIBLE
                binding.imgSelected.visibility = View.VISIBLE
                binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
                binding.btnImageUpload.visibility = View.INVISIBLE
            } else {
                // position이 0, 1, 2 등일 경우 해당 voteItem에 이미지 로드
                voteItemList[currentPosition].imageUrl = photoUri.toString()
                voteItemAdapter.notifyItemChanged(currentPosition)
            }

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
                if (currentPosition == -1) {
                    // position이 -1이면 binding.imgSelected에 사진 로드
                    binding.imgSelected.setImageURI(it)
                    viewModel.imageUrl = it.toString()
                    binding.imageContainer.visibility = View.VISIBLE
                    binding.imgSelected.visibility = View.VISIBLE
                    binding.btnRemoveImage.visibility = View.VISIBLE // x 버튼 표시
                    binding.btnImageUpload.visibility = View.INVISIBLE
                } else {
                    // position이 0, 1, 2 등일 경우 해당 voteItem에 이미지 로드
                    voteItemList[currentPosition].imageUrl = it.toString()
                    voteItemAdapter.notifyItemChanged(currentPosition)
                }
            } else {
                Toast.makeText(requireContext(), "이미지 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoOptions(position: Int) {
        currentPosition = position
        val votePopup = VotePopupDialog(
            onTakePhoto = { takePhoto(position) },
            onChoosePhoto = { selectImageFromGallery(position) },
            onLoadItem = {
                val action = CreateVoteFragmentDirections.actionCreateVoteFragmentToLoadCategoryFragment(position, viewModel.username, -1)
                findNavController().navigate(action) },
            onCancel = { /* 취소 버튼 동작 */ }
        )
        votePopup.show(parentFragmentManager, "VotePopupDialog")
    }


    private fun takePhoto(position: Int) {
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


    private fun selectImageFromGallery(position: Int) {
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

    fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
//        val layoutManager = recyclerView.layoutManager ?: return

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
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
