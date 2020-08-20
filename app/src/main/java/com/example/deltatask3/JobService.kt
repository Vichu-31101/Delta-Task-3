package com.example.deltatask3

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class JobService : JobService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d("main", "Job started")
        doInBackgroud(params)
        return true
    }

    fun doInBackgroud(params: JobParameters){

        var num = PreferenceManager.getDefaultSharedPreferences(this).getString("num", "0")
        var sms = PreferenceManager.getDefaultSharedPreferences(this).getString("sms","")
        if(!sms.isNullOrEmpty() && !num.isNullOrEmpty()){
            Log.d("main", "works")
            val smsManager = SmsManager.getDefault() as SmsManager
            smsManager.sendTextMessage("+$num", null, sms, null, null)
        }
        jobFinished(params,false)
    }


    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

}