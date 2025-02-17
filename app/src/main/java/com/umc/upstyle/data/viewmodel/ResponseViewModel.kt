package com.umc.upstyle.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.umc.upstyle.data.model.ClothIdResponse
import com.umc.upstyle.data.model.ClothRequestDesDTO

class ResponseViewModel : ViewModel() {

    private val _imageUris = MutableLiveData<List<String>>(emptyList())
    val imageUris: LiveData<List<String>> = _imageUris

    private val _clothList = MutableLiveData<MutableList<ClothRequestDesDTO>>()
    val clothList: LiveData<MutableList<ClothRequestDesDTO>> = _clothList

    private val _clothIDList = MutableLiveData<MutableList<ClothIdResponse>>()
    val clothIDList: LiveData<MutableList<ClothIdResponse>> = _clothIDList


    init {
        resetData()
    }

    fun addImage(uri: String) {
        _imageUris.value = _imageUris.value?.plus(uri)
    }

    fun addClothRequest(cloth: ClothRequestDesDTO) {
        // 현재 리스트를 가져옴
        val currentList = _clothList.value ?: mutableListOf()
        val currentIDList = _clothIDList.value ?: mutableListOf()

        // 이미 존재하는 아이템을 찾고 삭제
        val existingIndex = currentList.indexOfFirst { it.clothKindId == cloth.clothKindId }
        if (existingIndex != -1) {
            currentList.removeAt(existingIndex)
        }
        // 이미 존재하는 아이템을 찾고 삭제
        val existingIDIndex = currentList.indexOfFirst { it.clothId == cloth.clothId }
        if (existingIDIndex != -1) {
            currentIDList.removeAt(existingIDIndex)
        }

        // 새로운 아이템을 리스트에 추가
        currentList.add(cloth)

        // ClothIdResponse 객체 생성
        val newClothIdResponse = ClothIdResponse(cloth.clothId)

        // 새로운 아이템을 리스트에 추가
        currentIDList.add(newClothIdResponse)

        // 리스트 업데이트
        _clothList.value = currentList
        _clothIDList.value = currentIDList

    }



    // ✅ ViewModel의 데이터를 초기화하는 함수 추가
    fun clearData() {
        resetData()
    }

    private fun resetData() {
        _imageUris.value = emptyList()
        _clothList.value = mutableListOf()
        _clothIDList.value = mutableListOf()
    }
}
