package com.umc.upstyle

import com.umc.upstyle.utils.Item_load
import android.content.Context
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
import com.bumptech.glide.Glide
import com.umc.upstyle.data.model.AddCodiReqDTO
import com.google.firebase.storage.FirebaseStorage
import com.umc.upstyle.data.network.RequestService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.viewmodel.RequestViewModel
import com.umc.upstyle.databinding.FragmentCreateRequestBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class CreateRequestFragment : Fragment() {
    private var _binding: FragmentCreateRequestBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null // ✅ lateinit 제거 및 nullable 변경

    private lateinit var viewModel: RequestViewModel
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRequestBinding.inflate(inflater, container, false)

        editTextTitle = binding.etTitle
        editTextContent = binding.etContent


        // ViewModel 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(RequestViewModel::class.java)

        // ViewModel에 저장된 데이터가 있으면 복원
        editTextTitle.setText(viewModel.requestTitle)
        editTextContent.setText(viewModel.requestContent)

        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

                            photoUri = Uri.parse(item.imageUrl)
                            Glide.with(requireContext())
                                .load(item.imageUrl)
                                .into(binding.imgSelected)
                        }
                }
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

        binding.btnUpload.setOnClickListener {
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


    private suspend fun sendToServerWithFirebaseUpload() {
        val imageUrl = if (photoUri != null && !photoUri.toString().startsWith("http")) {
            uploadImageWithOverlay(photoUri!!)
        } else {
            photoUri?.toString()
        }
        Log.d("check", "photoUri는 $photoUri")

        if (imageUrl == null) {
            Log.e("CodiRequest", "Firebase image upload failed. Image URL is null.")
        } else {
            Log.d("CodiRequest", "Firebase image uploaded successfully. URL: $imageUrl")
        }

        val request = AddCodiReqDTO(
            userId = 1,
            title = binding.etTitle.text.toString(),
            body = binding.etContent.text.toString(),
            imageUrl = imageUrl ?: ""
        )

        val requestService = RetrofitClient.createService(RequestService::class.java)
        try {
            val response = requestService.createCodiReq(request)
            if (response.isSuccessful) {
                findNavController().navigate(R.id.chatFragment)
                Log.d("CodiRequest", "업로드 성공")
            } else {
                Log.e("CodiRequest", "업로드 실패: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("CodiRequest", "서버 요청 중 예외 발생: ${e.message}", e)
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
                photoUri = it
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


    // AddCodiReqDTO 객체와 API 응답을 처리하는 함수
    private suspend fun sendToServer() {
        val requestService = RetrofitClient.createService(RequestService::class.java)
        val request = AddCodiReqDTO(
            userId = 1,
            title = binding.etTitle.text.toString(),
            body = binding.etContent.text.toString(),
            imageUrl = photoUri.toString()
        )

        try {
            // API 요청을 보냄
            val response = requestService.createCodiReq(request)

            // 응답이 성공적인지 확인
            if (response.isSuccessful) {
                // 성공적인 응답 처리
                val responseData = response.body()
                findNavController().navigate(R.id.chatFragment)
                Log.d("CodiRequest", "업로드 성공")
                // 응답이 null이 아닐 경우 처리
                if (responseData != null) {
                    Log.d("CodiRequest", "Codi created: ")
                } else {
                    // 응답 본문이 null일 경우 처리
                    Log.e("CodiRequest", "Empty response body")
                }
            } else {
                // 요청 실패 시 상태 코드 확인
                Log.e("CodiRequest", "Failed with code: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                Log.e("CodiRequest", "Error body: $errorBody")
            }
        } catch (e: Exception) {
            // 예외 처리
            e.printStackTrace()
            Log.e("CodiRequest", "Error: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
