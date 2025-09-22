package com.example.whilelist

import android.content.Context
import android.content.SharedPreferences

class WhiteListManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("WhiteListPrefs", Context.MODE_PRIVATE)

    fun addNumber(number: String) {
        val set = getWhiteList().toMutableSet()
        set.add(number)
        prefs.edit().putStringSet("white_list", set).apply()
    }

    fun removeNumber(number: String) {
        val set = getWhiteList().toMutableSet()
        set.remove(number)
        prefs.edit().putStringSet("white_list", set).apply()
    }

    fun getWhiteList(): Set<String> {
        return prefs.getStringSet("white_list", emptySet()) ?: emptySet()
    }
}