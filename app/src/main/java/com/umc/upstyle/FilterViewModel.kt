package com.umc.upstyle

import android.content.Context
import androidx.lifecycle.ViewModel
import com.umc.upstyle.SharedPreferencesUtils

class FilterViewModel : ViewModel() {

    var selectedCategory: String? = null
    var selectedSubCategory: String? = null
    var selectedFitSize: String? = null
    var selectedColor: String? = null

    var kindId: Int? = null
    var categoryId: Int? = null
    var fitId: Int? = null
    var colorId: Int? = null

    fun loadFromSharedPreferences(context: Context) {
        kindId = SharedPreferencesUtils.getKindId(context)
        categoryId = SharedPreferencesUtils.getCategoryId(context)
        fitId = SharedPreferencesUtils.getFitId(context)
        colorId = SharedPreferencesUtils.getColorId(context)

    }

    fun clearSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("FilterPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    fun saveToSharedPreferences(context: Context) {
        SharedPreferencesUtils.saveData(context, kindId, categoryId, fitId, colorId)
    }

    fun resetFilters(context: Context) {
        SharedPreferencesUtils.clearData(context)
        kindId = null
        categoryId = null
        fitId = null
        colorId = null
    }
}


