package com.example.deltatask3

import android.Manifest
import android.app.*

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.provider.ContactsContract
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0

    var currentDate = Calendar.getInstance()
    var setDate = Calendar.getInstance()
    var timeDiff = 0L
    var selNumber = ""
    var sms = ""
    var permissionGranted = false
    var fromContact = false
    var schedulerType = 0


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTime.setOnClickListener {
            currentDate = Calendar.getInstance()
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, this@MainActivity, year, month,day)
            datePickerDialog.show()
        }

        startSchedule.setOnClickListener {
            permissionCheck()
            sms = smsMessage.text.toString()
            if((!sms.isNullOrEmpty()) && selNumber.isNotEmpty() && timeDiff > 0){
                when (schedulerType) {
                    0 -> {
                        workScheduler()
                    }
                    1 -> {
                        scheduler()
                    }
                    else -> {
                        AlarmScheduler()
                    }
                }

            }
            else{
                Toast.makeText(this,"Please enter valid details",Toast.LENGTH_SHORT).show()
            }
        }

        type.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val typeList = mutableListOf<String>("Work Manager","Job Scheduler","Alarm Manager")
            builder.setTitle("Pick Type")


            val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line, typeList
            )
            builder.setAdapter(
                dataAdapter
            ) { _, index ->
                schedulerType = index
                typeText.text = typeList[index]
            }
            val dialog = builder.create()
            dialog.show()
        }

        contact.setOnClickListener {
            if(!permissionGranted){
                fromContact = true
                permissionCheck()

            }
            else{
                getContacts()
            }
        }
    }

    //Job Scheduler
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scheduler() {
        Log.d("main", timeDiff.toString())
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("num",selNumber).commit()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("sms",sms).commit()
        val componentName = ComponentName(applicationContext, com.example.deltatask3.JobService::class.java)
        val info: JobInfo = JobInfo.Builder(123, componentName)
            .setMinimumLatency(timeDiff)
            .setOverrideDeadline(timeDiff+50)
            .build()
        val scheduler: JobScheduler =
            getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode: Int = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this,"Scheduled",Toast.LENGTH_SHORT).show()
            Log.d("main", "Job scheduled")
        } else {
            Log.d("main", "Job scheduling failed")
        }
    }

    fun workScheduler(){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("num",selNumber).commit()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("sms",sms).commit()
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<SmsWorker>()
                .setInitialDelay(timeDiff,TimeUnit.MILLISECONDS)
                .build()
        WorkManager
            .getInstance(this)
            .enqueue(uploadWorkRequest)
        Toast.makeText(this,"Scheduled",Toast.LENGTH_SHORT).show()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun AlarmScheduler() {
        Toast.makeText(this,"Scheduled",Toast.LENGTH_SHORT).show()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("num",selNumber)
        intent.putExtra("sms",sms)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis+timeDiff, pendingIntent)
    }

    //Setting Date and Time
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        setDate.set(myYear,myMonth,myDay,myHour,myMinute)
        timeDiff = ChronoUnit.MILLIS.between(currentDate.toInstant(),setDate.toInstant())
        dateTimeText.text = ""+myHour + ":" + myMinute + " " +myDay+"/"+myMonth+"/"+myYear
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month
        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this@MainActivity, this@MainActivity, hour, minute, false)
        timePickerDialog.show()
    }

    //Permission Handling
    @RequiresApi(Build.VERSION_CODES.M)
    fun permissionCheck(){
        if (!(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS
                ), 1
            )
        }else{
            Log.d("main", fromContact.toString())
            if(fromContact){
                getContacts()

                fromContact = false
            }
            permissionGranted = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {

                permissionGranted = if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if(fromContact){
                        getContacts()
                        fromContact = false
                    }

                    true
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    false
                }
                return
            }
        }
    }

    fun getContacts(){
        val builder = AlertDialog.Builder(this)
        val nameList = mutableListOf<String>()
        val numList = mutableListOf<String>()
        builder.setTitle("Pick Contact")

        val contacts = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while(contacts!!.moveToNext())
        {
            if(!nameList.contains(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))){
                nameList.add(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                numList.add(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
            }
        }

        if(nameList.size == 0){
            nameList.add("You have no contacts")
        }

        val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, nameList
        )
        builder.setAdapter(
            dataAdapter
        ) { _, index ->
            selNumber = numList[index]
            contactText.text = nameList[index] + ": \n" + numList[index]
        }
        val dialog = builder.create()
        dialog.show()
    }


}


