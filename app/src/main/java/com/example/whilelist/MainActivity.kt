package com.example.whilelist

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NumberAdapter
    private val whiteListManager by lazy { WhiteListManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация UI
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NumberAdapter(whiteListManager.getWhiteList().toList()) { number ->
            whiteListManager.removeNumber(number)
            updateList()
        }
        recyclerView.adapter = adapter

        val editTextNumber: EditText = findViewById(R.id.editTextNumber)
        val buttonAdd: Button = findViewById(R.id.buttonAdd)
        buttonAdd.setOnClickListener {
            val number = editTextNumber.text.toString().trim()
            if (number.isNotEmpty()) {
                whiteListManager.addNumber(number)
                editTextNumber.text.clear()
                updateList()
            }
        }

        val buttonRequestRole: Button = findViewById(R.id.buttonRequestRole)
        buttonRequestRole.setOnClickListener {
            requestCallRedirectionRole()
        }

        // По умолчанию добавляем ваши номера, если список пуст
        if (whiteListManager.getWhiteList().isEmpty()) {
            whiteListManager.addNumber("+359886823754")
            whiteListManager.addNumber("+359886457705")
            updateList()
        }

        // Проверяем роль при запуске
        if (!isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)) {
            requestCallRedirectionRole()
        }
    }

    private fun updateList() {
        adapter.updateList(whiteListManager.getWhiteList().toList())
    }

    private fun requestCallRedirectionRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
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
                Toast.makeText(this, "Роль не назначена", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ROLE_REQUEST_CODE = 1
    }
}