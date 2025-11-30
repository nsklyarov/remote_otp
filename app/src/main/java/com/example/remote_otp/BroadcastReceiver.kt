package com.example.remote_otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.net.URL

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS)
            if (status != null) {
                val sms = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                if (!sms.isNullOrEmpty()) {
                    Log.d("SMS_RETRIEVER", "Got SMS: $sms")
                    // Отправляем в Telegram
                    CoroutineScope(Dispatchers.IO).launch {
                        sendToTelegram(context, sms)
                    }
                }
            }
        }
    }

    private fun sendToTelegram(context: Context, msg: String) {
        try {
            val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
            val bot = prefs.getString("bot_token", "") ?: ""
            val chat = prefs.getString("chat_id", "") ?: ""
            val enc = URLEncoder.encode(msg, "UTF-8")
            val url = "https://api.telegram.org/bot$bot/sendMessage?chat_id=$chat&text=$enc"
            Log.d("SMS_SERVICE", "Sending to Telegram: $url")
            URL(url).openConnection().apply { connect() }
        } catch (e: Exception) {
            Log.e("SMS_SERVICE", "Error sending to Telegram: ${e.message}")
        }
    }
}
