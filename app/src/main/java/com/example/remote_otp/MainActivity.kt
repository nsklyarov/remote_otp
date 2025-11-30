package com.example.remote_otp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val SMS_PERMISSIONS_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)

        val etBotToken = findViewById<EditText>(R.id.etBotToken)
        val etChatId = findViewById<EditText>(R.id.etChatId)
        val btnSaveConfig = findViewById<Button>(R.id.btnSaveConfig)
        val tvBotToken = findViewById<TextView>(R.id.tvBotToken)
        val tvChatId = findViewById<TextView>(R.id.tvChatId)

        // Заполняем поля сохранёнными значениями
        etBotToken.setText(prefs.getString("bot_token", ""))
        etChatId.setText(prefs.getString("chat_id", ""))
        tvBotToken.text = "Current Bot Token: ${prefs.getString("bot_token", "")}"
        tvChatId.text = "Current Chat ID: ${prefs.getString("chat_id", "")}"

        // Кнопка сохранения
        btnSaveConfig.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("bot_token", etBotToken.text.toString())
            editor.putString("chat_id", etChatId.text.toString())
            editor.apply()

            // Обновляем TextView после сохранения
            tvBotToken.text = "Current Bot Token: ${etBotToken.text}"
            tvChatId.text = "Current Chat ID: ${etChatId.text}"
        }

        // Проверяем разрешения
        if (!hasSmsPermissions()) {
            requestSmsPermissions()
        } else {
            Log.d("PERMISSIONS", "SMS permissions already granted")
        }
    }

    private fun hasSmsPermissions(): Boolean {
        val receiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        return receiveSms == PackageManager.PERMISSION_GRANTED && readSms == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ),
            SMS_PERMISSIONS_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSIONS_REQUEST) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("PERMISSIONS", "SMS permissions granted")
            } else {
                Log.d("PERMISSIONS", "SMS permissions denied")
            }
        }
    }
}
