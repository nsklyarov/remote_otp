package com.example.remote_otp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private val SMS_PERMISSIONS_REQUEST = 101

    private lateinit var etBotToken: EditText
    private lateinit var etChatId: EditText
    private lateinit var btnSaveConfig: Button
    private lateinit var tvBotToken: TextView
    private lateinit var tvChatId: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)

        etBotToken = findViewById(R.id.etBotToken)
        etChatId = findViewById(R.id.etChatId)
        btnSaveConfig = findViewById(R.id.btnSaveConfig)
        tvBotToken = findViewById(R.id.tvBotToken)
        tvChatId = findViewById(R.id.tvChatId)

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

            tvBotToken.text = "Current Bot Token: ${etBotToken.text}"
            tvChatId.text = "Current Chat ID: ${etChatId.text}"
        }

        // Проверяем разрешения SMS
        if (!hasSmsPermissions()) {
            requestSmsPermissions()
        } else {
            Log.d("PERMISSIONS", "SMS permissions already granted")
        }

        // Стартуем SMS Retriever
        startSmsListener()
    }

    private fun hasSmsPermissions(): Boolean {
        val receiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        return receiveSms == PackageManager.PERMISSION_GRANTED && readSms == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
            SMS_PERMISSIONS_REQUEST
        )
    }

    private fun startSmsListener() {
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            Log.d("SMS_RETRIEVER", "SMS Retriever started successfully")
        }
        task.addOnFailureListener {
            Log.e("SMS_RETRIEVER", "Failed to start SMS Retriever: ${it.message}")
        }
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
