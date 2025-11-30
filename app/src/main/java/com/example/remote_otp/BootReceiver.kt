package com.example.remote_otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        if (p1.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(p0, SmsService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                p0.startForegroundService(serviceIntent)
            } else {
                p0.startService(serviceIntent)
            }

        }
    }
}