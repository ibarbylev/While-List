package com.example.whilelist

import android.net.Uri
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class WhileListRedirectionService : CallRedirectionService() {

    private val TAG = "WhileListService"
    private val whiteListManager by lazy { WhiteListManager(this) }

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        val dialedNumber = handle.schemeSpecificPart.trim().replace("+", "")  // Нормализация
        val whiteList = whiteListManager.getWhiteList().map { it.trim().replace("+", "") }

        Log.d(TAG, "Попытка вызова: $dialedNumber")

        if (whiteList.any { dialedNumber == it || dialedNumber.endsWith(it) || it.endsWith(dialedNumber) }) {  // Простая проверка на совпадение
            placeCallUnmodified()  // Разрешаем вызов
        } else {
            cancelCall()  // Блокируем максимально быстро
            Log.d(TAG, "Вызов заблокирован: $dialedNumber не в белом списке")
        }
    }
}