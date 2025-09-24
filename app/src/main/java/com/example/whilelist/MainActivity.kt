package com.example.whilelist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whilelist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null  // Nullable для безопасности
    private var adapter: NumberAdapter? = null
    private val whiteListManager by lazy { WhiteListManager(this) }
    private val TAG = "MainActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding?.root)
            Log.d(TAG, "Binding inflated")

            binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            Log.d(TAG, "LayoutManager set")
            adapter = NumberAdapter(whiteListManager.getWhiteList().toList()) { number ->
                whiteListManager.removeNumber(number)
                updateList()
            }
            binding?.recyclerView?.adapter = adapter
            Log.d(TAG, "Adapter set")

            binding?.buttonAdd?.setOnClickListener {
                val number = binding?.editTextNumber?.text.toString().trim()
                if (number.isNotEmpty()) {
                    whiteListManager.addNumber(number)
                    binding?.editTextNumber?.text?.clear()
                    updateList()
                }
            }
            Log.d(TAG, "buttonAdd listener set")

            binding?.buttonRequestAccessibility?.setOnClickListener {
                requestAccessibilityPermission()
            }
            Log.d(TAG, "buttonRequestAccessibility listener set")

            if (whiteListManager.getWhiteList().isEmpty()) {
                whiteListManager.addNumber("+359886823754")
                whiteListManager.addNumber("+359886457705")
                updateList()
            }
            Log.d(TAG, "Default numbers added if empty")

            if (!isAccessibilityServiceEnabled()) {
                requestAccessibilityPermission()
            }
            Log.d(TAG, "Accessibility check done")
        } catch (e: Exception) {
            Log.e(TAG, "Краш в onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка запуска: ${e.message}", Toast.LENGTH_LONG).show()
            finish()  // Закрыть activity при краше
        }
    }

    private fun updateList() {
        adapter?.updateList(whiteListManager.getWhiteList().toList())
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Включите While List в Accessibility", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Accessibility уже включен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.id.contains(packageName) }
    }
}