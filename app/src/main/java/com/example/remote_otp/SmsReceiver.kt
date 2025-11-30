package com.example.remote_otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SMS_DEBUG", "Received intent: ${intent.action}")

        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") {
            Log.d("SMS_DEBUG", "Intent is not SMS_RECEIVED, ignoring")
            return
        }

        val bundle: Bundle? = intent.extras
        if (bundle == null) {
            Log.d("SMS_DEBUG", "No extras in intent")
            return
        }

        val pdus = bundle["pdus"] as? Array<*>
        if (pdus == null || pdus.isEmpty()) {
            Log.d("SMS_DEBUG", "No PDUs found")
            return
        }

        val msgs = pdus.mapNotNull {
            try {
                SmsMessage.createFromPdu(it as ByteArray)
            } catch (e: Exception) {
                Log.e("SMS_DEBUG", "Failed to create SMS from PDU", e)
                null
            }
        }

        if (msgs.isEmpty()) {
            Log.d("SMS_DEBUG", "No valid SMS messages")
            return
        }

        val fullText = msgs.joinToString("\n") { it.displayMessageBody }
        Log.d("SMS_DEBUG", "Full SMS text: $fullText")

        val serviceIntent = Intent(context, SmsService::class.java)
        serviceIntent.putExtra("sms_text", fullText)

        try {
            context.startService(serviceIntent)
            Log.d("SMS_DEBUG", "Started SmsService with SMS text")
        } catch (e: Exception) {
            Log.e("SMS_DEBUG", "Failed to start SmsService", e)
        }
    }
}
