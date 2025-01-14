package com.umc.upstyle

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item_search(
    val description: String,
    val imageUrl: String
) : Parcelable
