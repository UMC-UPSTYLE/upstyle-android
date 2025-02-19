package com.umc.upstyle

import android.content.Context

object SharedPreferencesUtils {

    // ✅ 네 개의 프래그먼트에서만 사용할 SharedPreferences 이름
    private const val PREF_NAME = "FilterPrefsForFourFragments"

    // ✅ 데이터 저장 함수
    fun saveData(context: Context, kindId: Int?, categoryId: Int?, fitId: Int?, colorId: Int?) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            kindId?.let { putInt("kindId", it) }
            categoryId?.let { putInt("categoryId", it) }
            fitId?.let { putInt("fitId", it) }
            colorId?.let { putInt("colorId", it) }
            apply()  // ✅ 비동기 저장
        }
    }

    // ✅ kindId 읽기
    fun getKindId(context: Context): Int? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val kindId = sharedPref.getInt("kindId", -1)
        return if (kindId != -1) kindId else null
    }

    // ✅ categoryId 읽기
    fun getCategoryId(context: Context): Int? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val categoryId = sharedPref.getInt("categoryId", -1)
        return if (categoryId != -1) categoryId else null
    }

    // ✅ fitId 읽기
    fun getFitId(context: Context): Int? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val fitId = sharedPref.getInt("fitId", -1)
        return if (fitId != -1) fitId else null
    }

    // ✅ colorId 읽기
    fun getColorId(context: Context): Int? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val colorId = sharedPref.getInt("colorId", -1)
        return if (colorId != -1) colorId else null
    }

    // ✅ 데이터 초기화 함수
    fun clearData(context: Context) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}
