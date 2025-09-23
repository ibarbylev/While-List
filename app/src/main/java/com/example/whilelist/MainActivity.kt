package com.example.whilelist

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whilelist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NumberAdapter
    private val whiteListManager by lazy { WhiteListManager(this) }
    private val TAG = "MainActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            adapter = NumberAdapter(whiteListManager.getWhiteList().toList()) { number ->
                whiteListManager.removeNumber(number)
                updateList()
            }
            binding.recyclerView.adapter = adapter

            binding.buttonAdd.setOnClickListener {
                val number = binding.editTextNumber.text.toString().trim()
                if (number.isNotEmpty()) {
                    whiteListManager.addNumber(number)
                    binding.editTextNumber.text.clear()
                    updateList()
                }
            }

            binding.buttonRequestRole.setOnClickListener {
                requestCallRedirectionRole()
            }

            // Новая кнопка для accessibility (добавьте в layout или используйте существующую)
            // Для простоты: Добавьте в onCreate запрос, если redirection не сработал
            if (!isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)) {
                requestAccessibilityPermission()
            }

            if (whiteListManager.getWhiteList().isEmpty()) {
                whiteListManager.addNumber("+359886823754")
                whiteListManager.addNumber("+359886457705")
                updateList()
            }

            if (!isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)) {
                requestCallRedirectionRole()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Краш в onCreate: ${e.message}", e)
            Toast.makeText(this, "Ошибка запуска: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateList() {
        adapter.updateList(whiteListManager.getWhiteList().toList())
    }

    private fun requestCallRedirectionRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            Log.d(TAG, "Роль доступна: ${roleManager.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION)}")
            Log.d(TAG, "Роль назначена: ${roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)}")
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_REDIRECTION)
                startActivityForResult(intent, ROLE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Роль уже назначена или недоступна", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Включите While List в Accessibility", Toast.LENGTH_LONG).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.id.contains(packageName) }
    }

    private fun isRoleHeld(roleName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            return roleManager.isRoleHeld(roleName)
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ROLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Роль назначена успешно!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Роль не назначена (код $resultCode)", Toast.LENGTH_SHORT).show()  // Покажет код ошибки
            }
        }
    }

    companion object {
        private const val ROLE_REQUEST_CODE = 1
    }
}