package com.example.whilelist

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhileListAccessibilityService : AccessibilityService() {

    private val TAG = "AccessibilityService"
    private val whiteListManager by lazy { WhiteListManager(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val rootNode = rootInActiveWindow ?: return
            val dialedNumber = findDialedNumber(rootNode)  // Найти текст номера в dialer
            if (dialedNumber != null && isCallButtonPressed(event)) {
                val normalizedNumber = dialedNumber.replace(Regex("[^0-9]"), "")
                val whiteList = whiteListManager.getWhiteList().map { it.replace(Regex("[^0-9]"), "") }
                if (!whiteList.any { normalizedNumber.contains(it) || it.contains(normalizedNumber) }) {
                    performGlobalAction(GLOBAL_ACTION_BACK)  // Симулировать back для отмены
                    Log.d(TAG, "Вызов заблокирован accessibility: $normalizedNumber")
                }
            }
        }
    }

    private fun findDialedNumber(node: AccessibilityNodeInfo): String? {
        if (node.text != null && node.text.toString().matches(Regex("\\+?\\d{3,15}"))) {
            return node.text.toString()
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findDialedNumber(child)
            if (result != null) return result
        }
        return null
    }

    private fun isCallButtonPressed(event: AccessibilityEvent): Boolean {
        // Простая проверка: Если событие от кнопки вызова (ID или class)
        return event.className?.contains("Button") == true && event.text?.contains("Call") == true  // Адаптировать под dialer
    }

    override fun onInterrupt() {
        // Необходимый метод
    }
}