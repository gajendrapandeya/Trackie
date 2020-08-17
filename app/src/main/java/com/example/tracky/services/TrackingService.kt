package com.example.tracky.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.tracky.R
import com.example.tracky.other.Constants
import com.example.tracky.other.Constants.ACTION_PAUSE_SERVICE
import com.example.tracky.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.tracky.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.tracky.other.Constants.ACTION_STOP_SERVICE
import com.example.tracky.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.tracky.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.tracky.other.Constants.NOTIFICATION_ID
import com.example.tracky.ui.MainActivity
import timber.log.Timber

//Usually when we create a service, then that service class inherits either from Service or IntentService class
//But we are inheriting from LifeCycleService() because we need to observe from liveData object inside this class
//Observe() of LiveData object needs the lifeCycleOwner and if we don't specify LifecycleService() here
//then we cannot pass an instance of this service as a lifeCycleOwner to that observe function
class TrackingService: LifecycleService() {

    private var isFirstRun = true

    //this function get called whenEver we send a command to our service
    //i.e. when we send an intent with an action attached to this service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action) {

                ACTION_START_OR_RESUME_SERVICE -> {
                   if(isFirstRun) {
                       startForegroundService()
                       isFirstRun = false
                   } else {
                       Timber.d("Resuming Service")
                   }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //WhenEver we want a notification, we must first create a notification channel for that
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {

        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    //function to start a foreground service
    private fun startForegroundService() {

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                //if the user clicks on the notifications, then notification won't be disappeared
            .setAutoCancel(false)
                //notification can't be swiped away
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Trackie")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        //foreground service will be started
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        //A pending intent is to open our mainActivity when user clicks on notifications
        //We want to navigate user directly to TrackingFragment but by default it opens to MainActivity
        //for us that will be our RunFragment
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )


}