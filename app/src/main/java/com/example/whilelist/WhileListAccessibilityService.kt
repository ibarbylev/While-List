package com.example.whilelist

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class WhileListAccessibilityService : AccessibilityService() {

    private val TAG = "AccessibilityService"
    private val whiteListManager by lazy { WhiteListManager(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val rootNode = rootInActiveWindow ?: return
            val dialedNumber = findDialedNumber(rootNode)
            if (dialedNumber != null && isCallButtonEvent(event)) {
                val normalizedNumber = dialedNumber.replace(Regex("[^0-9]"), "")
                val whiteList = whiteListManager.getWhiteList().map { it.replace(Regex("[^0-9]"), "") }
                if (!whiteList.any { normalizedNumber == it || normalizedNumber.endsWith(it) || it.endsWith(normalizedNumber) }) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.d(TAG, "Вызов заблокирован accessibility: $normalizedNumber")
                    Toast.makeText(this, "Вызов заблокирован!", Toast.LENGTH_SHORT).show()  // Для теста
                }
            }
        }
    }

    private fun findDialedNumber(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
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

    private fun isCallButtonEvent(event: AccessibilityEvent): Boolean {
        return event.className?.contains("Button") == true && (event.text?.contains("Call") == true || event.text?.contains("Вызов") == true || event.text?.contains("Позвонить") == true)  // Улучшено для MIUI русского
    }

    override fun onInterrupt() {
        // Необходимый метод
    }
}