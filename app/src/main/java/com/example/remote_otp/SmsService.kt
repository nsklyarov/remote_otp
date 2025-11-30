package com.example.remote_otp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.os.Build

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.net.URLEncoder
import java.net.URL

class SmsService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground service для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "sms_service_channel"
            val channelName = "SMS Service"
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)

            val notification = Notification.Builder(this, channelId)
                .setContentTitle("Remote OTP Service")
                .setContentText("Running in background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

            startForeground(1, notification)
        }

        val text = intent?.getStringExtra("sms_text")
        if (text.isNullOrEmpty()) {
            Log.d("SMS_SERVICE", "No SMS text received")
            return START_NOT_STICKY
        }

        // Логируем полученный текст
        Log.d("SMS_SERVICE", "Got SMS: $text")

        CoroutineScope(Dispatchers.IO).launch {
            sendToTelegram(text)
        }

        return START_STICKY
    }

    private fun sendToTelegram(msg: String) {
        try {
            // Получаем токен и чат из SharedPreferences
            val prefs = getSharedPreferences("config", MODE_PRIVATE)
            val bot = prefs.getString("bot_token", null)
            val chat = prefs.getString("chat_id", null)

            if (bot.isNullOrEmpty() || chat.isNullOrEmpty()) {
                Log.e("SMS_SERVICE", "Bot token or chat ID not set")
                return
            }

            val enc = URLEncoder.encode(msg, "UTF-8")
            val urlString = "https://api.telegram.org/bot$bot/sendMessage?chat_id=$chat&text=$enc"
            Log.d("SMS_SERVICE", "Request URL: $urlString")

            val url = URL(urlString)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream.bufferedReader().use { it.readText() }

            Log.d("SMS_SERVICE", "Telegram response code: $code")
            Log.d("SMS_SERVICE", "Telegram response body: $resp")

            conn.disconnect()
        } catch (e: Exception) {
            Log.e("SMS_SERVICE", "Error sending to Telegram: ${e.message}")
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null
}
