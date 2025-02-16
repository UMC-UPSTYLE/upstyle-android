package com.umc.upstyle

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.umc.upstyle.data.model.ClosetCategoryResponse
import com.umc.upstyle.data.model.ClothRequestDTO
import com.umc.upstyle.data.model.OOTDRequest
import com.umc.upstyle.data.network.ApiService
import com.umc.upstyle.data.network.OOTDService
import com.umc.upstyle.data.network.RetrofitClient
import com.umc.upstyle.data.viewmodel.ClothViewModel
import com.umc.upstyle.databinding.ActivityTodayOotdBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TodayOotdFragment : Fragment(R.layout.activity_today_ootd) {

    private var _binding: ActivityTodayOotdBinding? = null
    private val binding get() = _binding!!

    // ViewModel을 프래그먼트 간 공유하기 위해 activityViewModels 사용
    private val clothViewModel: ClothViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityTodayOotdBinding.bind(view)

        // 이전 Fragment나 Activity에서 전달된 데이터 처리
        handleReceivedData()

        clothViewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }


        // 전달된 데이터 처리
        observeSelectedItem()

        // 상단에 띄우는 날짜
        val dateFormat = SimpleDateFormat("MMdd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())
        binding.date.text = todayDate

        // 저장된 데이터 복원
        updateUIWithViewModel()

        // 카테고리 버튼 이벤트 설정
        setupCategoryButtons()

        // 사진 등록 버튼 이벤트
        binding.photoUploadFrame.setOnClickListener { showPhotoOptions() }

        // 저장 버튼 이벤트
        binding.saveButton.setOnClickListener {
            val dateServer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ootdRequest = OOTDRequest(
                userId = 1,
                date = dateServer,
                imageUrls = mutableListOf(),  // 이미지 URL은 아래에서 추가
                clothRequestDTOList = clothViewModel.clothList.value ?: emptyList()
            )

            val imageUri = clothViewModel.imageUris.value?.lastOrNull()?.let { Uri.parse(it) }
            if (imageUri != null) {
                uploadOOTD(ootdRequest, imageUri)
            } else {
                Toast.makeText(requireContext(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로가기 버튼 클릭 이벤트 설정
        binding.backButton.setOnClickListener {

            // 정말 나가시겠습니까? 버튼 하나 추가하면 어떨지...
            clothViewModel.clearData()
            findNavController().navigate(R.id.mainFragment)
        }
    }

    private fun observeSelectedItem() {
        val navBackStackEntry = findNavController().currentBackStackEntry
        navBackStackEntry?.savedStateHandle?.getLiveData<String>("SELECTED_ITEM")?.observe(
            viewLifecycleOwner
        ) { selectedItem ->
            val category = navBackStackEntry.savedStateHandle.get<String>("CATEGORY")
//
            // ViewModel에 데이터 추가
            clothViewModel.updateCategory(category ?: "OTHER", selectedItem)
//            imageUrl?.let { clothViewModel.addImage(it) }
            val clothId = navBackStackEntry.savedStateHandle.get<Int>("CLOTH_ID") ?: 0
            val kindId = navBackStackEntry.savedStateHandle.get<Int>("KIND_ID") ?: 0
            val categoryId = navBackStackEntry.savedStateHandle.get<Int>("CATEGORY_ID") ?: 0
            val fitId = navBackStackEntry.savedStateHandle.get<Int>("FIT_ID") ?: 0
            val colorId = navBackStackEntry.savedStateHandle.get<Int>("COLOR_ID") ?: 0
            val addInfo = navBackStackEntry.savedStateHandle.get<String>("ADD_INFO") ?: ""



            if(clothId != 0) {
                // DTO 생성
                val clothRequestDTO = ClothRequestDTO(
                    clothId = clothId,
                    clothKindId = kindId,
                    clothCategoryId = categoryId,
                    fitCategoryId = fitId,
                    colorCategoryId = colorId,
                    additionalInfo = addInfo
                )

                clothViewModel.addClothRequest(clothRequestDTO)

            }
            // UI 업데이트
            updateCategoryUI(category, selectedItem)
        }
    }




    private fun uploadOOTD(request: OOTDRequest, imageUri: Uri?) {
        lifecycleScope.launch {
            try {
                val uploadedImageUrls = mutableListOf<String>()

                // 이미지 URI가 있으면 Firebase에 업로드
                if (imageUri != null) {
                    // 이미지 업로드가 완료되면 uploadedImageUrls를 업데이트
                    val imageUrl = uploadImageToFirebase(imageUri)
                    if (imageUrl != null) {
                        uploadedImageUrls.add(imageUrl)
                        Log.d("Firebase", "이미지 업로드 성공: $imageUrl")
                    } else {
                        Log.e("Firebase", "이미지 업로드 실패")
                    }
                }

                // 만약 이미지 업로드에 실패했다면 바로 종료
                if (uploadedImageUrls.isEmpty()) {
                    Log.e("Upload", "이미지 업로드에 실패했습니다.")
                    return@launch
                }

                // 업로드한 이미지 URL로 request 객체 업데이트
                val updatedRequest = request.copy(imageUrls = uploadedImageUrls)

                Log.d("Request Update", "Request Update 반영 성공")
                // 서버로 요청 전송
                Log.d("Retrofit", "Retrofit 서버 전달 시도")
                Log.d("Retrofit", "$updatedRequest")
                sendToServer(updatedRequest)
                Toast.makeText(requireContext(), "OOTD 업로드 성공", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_todayOotdFragment_to_mainActivity)

            } catch (e: Exception) {
                Log.e("Retrofit", "에러 발생: ${e.message}")
            }
        }
    }

    // Firebase에 이미지를 업로드하는 함수 (output으로 반환)
    private suspend fun uploadImageToFirebase(uri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)

            val uploadTask = imageRef.putFile(uri)

            // 업로드 진행 상황을 처리하려면, suspendCoroutine을 사용하여 비동기 작업을 처리할 수 있습니다.
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                // 진행률 표시 (예: ProgressBar)
                Log.d("FirebaseStorage", "Upload progress: $progress%")
            }

            // 업로드 성공 후 downloadUrl을 가져오기
            val downloadUri = suspendCoroutine<String?> { cont ->
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        cont.resume(uri.toString())
                    }.addOnFailureListener { exception ->
                        Log.e("FirebaseStorage", "Download URL Error", exception)
                        cont.resume(null)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseStorage", "Upload Error", exception)
                    cont.resume(null)
                }
            }

            downloadUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // 서버로 OOTDRequest를 전송하는 함수
    private fun sendToServer(updatedRequest: OOTDRequest) {
        val ootdService = RetrofitClient.createService(OOTDService::class.java)
        lifecycleScope.launch {
            try {
                Log.d("Retrofit", "서버 전송 시도")
                val response = ootdService.uploadOOTD(updatedRequest)

                if (response.isSuccess) {
                    Log.d("Retrofit", "업로드 성공!")
                } else {
                    Log.e("Retrofit", "업로드 실패: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "서버 요청 에러: ${e.message}")
            }
        }
    }




    // 이전 Fragment나 Activity에서 전달된 데이터 처리
    private fun handleReceivedData() {
        val selectedCategory = arguments?.getString("CATEGORY")
        val selectedSubCategory = arguments?.getString("SUB_CATEGORY")
        val selectedFit = arguments?.getString("FIT")
        val selectedSize = arguments?.getString("SIZE")
        val selectedColor = arguments?.getString("COLOR")
        val selectedEtc = arguments?.getString("ETC")

        val clothId = arguments?.getInt("CLOTH_ID") ?:0
        val kindId = arguments?.getInt("KIND_ID") ?:0
        val categoryId = arguments?.getInt("CATEGORY_ID") ?:0
        val fitId = arguments?.getInt("FIT_ID") ?:0
        val colorId = arguments?.getInt("COLOR_ID") ?:0
        val addInfo = arguments?.getString("ADD_INFO") ?:""


        // 새로 받아온 아이템 정보가 있다면
        if(kindId != 0) {
            // DTO 생성
            val clothRequestDTO = ClothRequestDTO(
                clothId = clothId,
                clothKindId = kindId,
                clothCategoryId = categoryId,
                fitCategoryId = fitId,
                colorCategoryId = colorId,
                additionalInfo = addInfo
            )

            clothViewModel.addClothRequest(clothRequestDTO)

        }

        if (!selectedCategory.isNullOrEmpty()) {
            val categoryText = when (selectedCategory) {
                "OUTER", "TOP", "BOTTOM" -> "$selectedSubCategory $selectedFit $selectedColor $selectedEtc"
                "SHOES", "OTHER" -> "$selectedSubCategory $selectedColor $selectedEtc"
                "BAG" -> "$selectedSubCategory $selectedSize $selectedColor $selectedEtc"
                else -> ""
            }


            // UI 업데이트 되도록
            clothViewModel.updateCategory(selectedCategory, categoryText)
        }
    }

    private fun setupCategoryButtons() {
        val buttons = listOf(
            binding.outerButton to "OUTER",
            binding.topButton to "TOP",
            binding.bottomButton to "BOTTOM",
            binding.shoesButton to "SHOES",
            binding.bagButton to "BAG",
            binding.otherButton to "OTHER"
        )

        buttons.forEach { (button, category) ->
            button.setOnClickListener {
                showLoadItemPopupDialog(category)
            }
        }
    }

    private fun showLoadItemPopupDialog(category: String) {
        val dialog = LoadItemPopupDialog(
            requireContext(),
            onCreateNewClicked = {
                // "새로 생성" 클릭 시 카테고리 생성 화면으로 이동
                navigateToCategoryFragment(category)
            },
            onLoadPreviousClicked = {
                // "불러오기" 클릭 시 LoadItemFragment로 이동
                navigateToLoadItemFragment(category)
            }
        )
        dialog.show()
    }


    //categoryFragment로 이동
    private fun navigateToCategoryFragment(category: String) {

        val bundle = Bundle().apply {
            putString("CATEGORY", category)
        }
        Log.d("TodayOotdFragment", "Navigating to CategoryFragment with category: $category") // 디버깅 로그 추가

        findNavController().navigate(R.id.categoryFragment, bundle)

    }

    // LoadItemFragment로 이동
    private fun navigateToLoadItemFragment(category: String) {
        val action = TodayOotdFragmentDirections.actionTodayOotdFragmentToLoadItemFragment(category)
        findNavController().navigate(action)
    }

    private fun updateCategoryUI(category: String?, itemName: String?) {
        when (category) {
            "OUTER" -> binding.outerText.text = itemName
            "TOP" -> binding.topText.text = itemName
            "BOTTOM" -> binding.bottomText.text = itemName
            "SHOES" -> binding.shoesText.text = itemName
            "BAG" -> binding.bagText.text = itemName
            "OTHER" -> binding.otherText.text = itemName
        }
    }


    private fun updateUIWithViewModel() {
        // 카테고리 UI 업데이트
        clothViewModel.categoryData.observe(viewLifecycleOwner) { categoryData ->
            binding.outerText.text = categoryData["OUTER"]
            binding.topText.text = categoryData["TOP"]
            binding.bottomText.text = categoryData["BOTTOM"]
            binding.shoesText.text = categoryData["SHOES"]
            binding.bagText.text = categoryData["BAG"]
            binding.otherText.text = categoryData["OTHER"]
        }

        // 이미지 UI 업데이트 수정
        clothViewModel.imageUris.observe(viewLifecycleOwner) { images ->
            if (images.isNotEmpty()) {
                binding.photoImageView.visibility = View.VISIBLE

                val lastImagePath = images.last() // 마지막 이미지 경로 가져오기
                val imageUri = Uri.parse(lastImagePath) // 문자열을 Uri로 변환
                binding.photoImageView.setImageURI(imageUri) // 이미지 뷰에 적용
            }
            updateSaveButtonVisibility()
        }

    }

    private fun updateSaveButtonVisibility() {
        val hasImage = clothViewModel.imageUris.value?.isNotEmpty() == true
        val hasCategory = clothViewModel.categoryData.value?.values?.any { it != "없음" } == true
        binding.saveButton.visibility = if (hasImage || hasCategory) View.VISIBLE else View.GONE
    }


    // 사진 관련 코드 시작
    private lateinit var photoUri: Uri // 사진 촬영 URI

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.photoImageView.visibility = View.VISIBLE
            binding.photoImageView.setImageURI(photoUri) // 촬영한 사진 표시
            binding.uploadText.visibility = View.GONE // "사진 등록" 텍스트 숨김
            saveImageUri(photoUri) // URI 저장
            Toast.makeText(requireContext(), "사진 촬영 성공!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "사진 촬영 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(it)
            if (savedPath != null) {
                binding.photoImageView.visibility = View.VISIBLE
                binding.photoImageView.setImageURI(Uri.parse(savedPath))
                binding.uploadText.visibility = View.GONE // "사진 등록" 텍스트 숨김
                clothViewModel.addImage(savedPath)
            } else {
                Toast.makeText(requireContext(), "이미지 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoOptions() {
        val photoPopup = PhotoPopupDialog(
            onTakePhoto = { takePhoto() },
            onChoosePhoto = { selectImageFromGallery() },
            onCancel = { /* 취소 버튼 동작 */ }
        )
        photoPopup.show(parentFragmentManager, "PhotoPopupDialog")
    }

    private fun takePhoto() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)

            // photoUri 초기화
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

    private fun saveImageUri(photoUri: Uri?) {
        if (photoUri != null) {
            val preferences = requireActivity().getSharedPreferences("AppData", Context.MODE_PRIVATE)
            clothViewModel.addImage(photoUri.toString())
            preferences.edit().putString("SAVED_IMAGE_PATH", photoUri.toString()).apply()
        } else {
            Toast.makeText(requireContext(), "이미지 경로 저장 실패: URI가 null입니다.", Toast.LENGTH_SHORT).show()
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

            //저장된 파일을 FileProvider를 사용하여 Uri 변환
            val savedUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            savedUri.toString() // Uri를 String으로 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun fetchClosetItems(categoryId: Int, category: String) {
        val apiService = RetrofitClient.createService(ApiService::class.java)

        apiService.getClosetByCategory(userId = 1, categoryId = categoryId)
            .enqueue(object : Callback<ClosetCategoryResponse> {
                override fun onResponse(
                    call: Call<ClosetCategoryResponse>,
                    response: Response<ClosetCategoryResponse>
                ) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val items = response.body()?.result?.clothPreviewList ?: emptyList()

                        if (items.isNotEmpty()) {
                            val firstItem = items.first()
                            updateCategoryUI(
                                category,
                                firstItem.kindName
                            )

                            // ViewModel에도 저장
                            clothViewModel.updateCategory(category, firstItem.kindName)
                            firstItem.ootd?.imageUrl?.let { clothViewModel.addImage(it) }
                        }
                    } else {
                        Toast.makeText(requireContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ClosetCategoryResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}