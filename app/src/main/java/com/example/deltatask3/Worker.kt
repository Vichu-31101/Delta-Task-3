package com.example.deltatask3

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class SmsWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    var context = appContext
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        doInBackgroud(context)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
    private fun doInBackgroud(context: Context){

        var num = PreferenceManager.getDefaultSharedPreferences(context).getString("num", "0")
        var sms = PreferenceManager.getDefaultSharedPreferences(context).getString("sms","")
        if(!sms.isNullOrEmpty() && !num.isNullOrEmpty()){
            Log.d("main", "works")
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("+$num", null, sms, null, null)
        }
    }
}