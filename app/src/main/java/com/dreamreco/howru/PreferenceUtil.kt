package com.dreamreco.howru

import android.content.Context
import android.content.SharedPreferences

// 기본 데이터 저장소
// 1. DB 존재하는 그룹명 리스트 key : group
// 2. Phone 에 저장된 연락처 갯수 key : contactsNumber - 미사용
class PreferenceUtil(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getInt(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun setInt(key: String, int: Int) {
        prefs.edit().putInt(key, int).apply()
    }

    fun removeString(key: String) {
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }
}