package com.example.deltatask3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            sendSMS(context,intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }
    fun sendSMS(context: Context,intent: Intent){
        val bundle = intent.extras;
        var num = bundle?.get("num") as String
        var sms = bundle?.get("sms") as String
        if(!sms.isNullOrEmpty() && !num.isNullOrEmpty()){
            Log.d("main", num)
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("+$num", null, sms, null, null)
        }

    }
}

