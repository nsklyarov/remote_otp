package com.example.remote_otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle: Bundle? = intent.extras
        if (bundle == null) return

        val pdus = bundle["pdus"] as Array<*>
        val msgs = pdus.map { SmsMessage.createFromPdu(it as ByteArray) }

        val fullText = msgs.joinToString("\n") { it.displayMessageBody }

        val serviceIntent = Intent(context, SmsService::class.java)
        serviceIntent.putExtra("sms_text", fullText)

        context.startService(serviceIntent)
    }
}
