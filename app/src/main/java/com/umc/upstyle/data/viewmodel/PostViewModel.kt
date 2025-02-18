package com.umc.upstyle.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.umc.upstyle.data.model.VoteOptionSimple
import com.umc.upstyle.data.model.VoteItem

class PostViewModel : ViewModel() {
    var postTitle: String = ""
    var postContent: String = ""
    var imageUrl: String = ""

    private val _optionList = MutableLiveData<MutableList<VoteOptionSimple>>()
    val optionList: LiveData<MutableList<VoteOptionSimple>> = _optionList

    private val _nameList = MutableLiveData<MutableList<VoteItem>?>()
    var nameList: MutableLiveData<MutableList<VoteItem>?> = _nameList

    init {
        resetData()
    }

    // 아이템의 이미지 URL을 업데이트하는 메서드
    fun updateImageAtPosition(position: Int, newImageUrl: String) {
        // nameList.value가 null이 아니고, position이 유효한지 확인
        val currentList = _nameList.value
        if (currentList != null && position in currentList.indices) {
            // 해당 position에 있는 VoteItem의 imageUrl을 새로운 URL로 업데이트
            currentList[position].imageUrl = newImageUrl
            // LiveData에 변경 사항 반영
            _nameList.value = currentList
            Log.d("PostViewModel", "Image URL updated at position $position: $newImageUrl")
        } else {
            Log.e("PostViewModel", "Invalid position or nameList is null")
        }
    }



    // ✅ ViewModel의 데이터를 초기화하는 함수 추가
    fun clearData() {
        resetData()
    }

    private fun resetData() {
        _optionList.value = mutableListOf()
    }


}
