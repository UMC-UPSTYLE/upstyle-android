package com.umc.upstyle.data.model

import com.google.gson.annotations.SerializedName

// ✅ 단일 북마크 요청 (북마크 추가/삭제) 응답
data class BookmarkResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: BookmarkItem? // ✅ 단일 북마크 객체 반환 가능
)

// ✅ 북마크 목록 조회 응답 (북마크 리스트 포함)
data class BookmarkListResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: BookmarkResult // ✅ 리스트를 포함한 응답
)

// ✅ 북마크 목록 정보
data class BookmarkResult(
    @SerializedName("bookmarkList") val bookmarkList: List<BookmarkItem>, // ✅ 여러 개의 북마크
    @SerializedName("listSize") val listSize: Int,
    @SerializedName("totalPage") val totalPage: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("isFirst") val isFirst: Boolean,
    @SerializedName("isLast") val isLast: Boolean,
    val userId: Int,
    val clothId: Int,
    val isBookmarked: Boolean
)

// ✅ 단일 북마크 아이템 (북마크 추가/삭제 시 반환)
data class BookmarkItem(
    @SerializedName("clothId") val clothId: Int,
    @SerializedName("kind") val kind: String,
    @SerializedName("category") val category: String,
    @SerializedName("fit") val fit: String,
    @SerializedName("color") val color: String,
    @SerializedName("ootd") val ootd: OotdItem?,
    @SerializedName("userId") val userId: Int,
    @SerializedName("isBookmarked") val isBookmarked: Boolean,
    @SerializedName("bookmarkList") val bookmarkList: List<BookmarkItem>
)

// ✅ OOTD 이미지 정보
data class OotdItem(
    @SerializedName("id") val id: Int,
    @SerializedName("imageUrl") val imageUrl: String
)
