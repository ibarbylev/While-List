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
        val dialedNumber = handle.schemeSpecificPart.replace(Regex("[^0-9]"), "")  // Лучшая нормализация: только цифры
        val whiteList = whiteListManager.getWhiteList().map { it.replace(Regex("[^0-9]"), "") }

        Log.d(TAG, "Попытка вызова: $dialedNumber (нормализованный)")

        if (whiteList.any { dialedNumber == it || dialedNumber.endsWith(it) || it.endsWith(dialedNumber) }) {
            placeCallUnmodified()  // Разрешаем
        } else {
            cancelCall()  // Блокируем
            Log.d(TAG, "Вызов заблокирован: $dialedNumber не в белом списке")
        }
    }
}