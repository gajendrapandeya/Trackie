package com.example.tracky.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ContentProviderClient
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tracky.R
import com.example.tracky.other.Constants.ACTION_PAUSE_SERVICE
import com.example.tracky.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.tracky.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.tracky.other.Constants.ACTION_STOP_SERVICE
import com.example.tracky.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.tracky.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.tracky.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.tracky.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.tracky.other.Constants.NOTIFICATION_ID
import com.example.tracky.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.tracky.other.TrackingUtility
import com.example.tracky.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

//Usually when we create a service, then that service class inherits either from Service or IntentService class
//But we are inheriting from LifeCycleService() because we need to observe from liveData object inside this class
//Observe() of LiveData object needs the lifeCycleOwner and if we don't specify LifecycleService() here
//then we cannot pass an instance of this service as a lifeCycleOwner to that observe function
class TrackingService : LifecycleService() {

    private var isFirstRun = true

    private lateinit var fuesdLocationProviderClient: FusedLocationProviderClient

    //time for notifications
    private val timeRunInSeconds = MutableLiveData<Long>()


    companion object {
        //If we are tracking user or not
        val isTracking = MutableLiveData<Boolean>()

        // this live data will hold list of list of co-ordinates that's because
        //suppose user was first running then he stopped the service (this will generate first line)
        //and if he again start the service(new line will be generated)
        val pathPoints = MutableLiveData<Polylines>()

        //time for tracking fragment
        val timeRunInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fuesdLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    //this function get called whenEver we send a command to our service
    //i.e. when we send an intent with an action attached to this service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {

                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service")
                        startTimer()
                    }
                }

                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseService()
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

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    //function to start a foreground service
    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    //function that post initial values to LiveData
    private fun postInitialValues() {
        isTracking.postValue(false)
        //empty list
        pathPoints.postValue(mutableListOf())

        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    //function that adds an empty polyline at the end of our polyLines list
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    //function that adds coordinates to the last polyline of our polyLines list
    private fun addPathPoints(location: Location?) {

        //if location is not null
        location?.let {

            //converting latLng to location
            val pos = LatLng(location.latitude, location.longitude)

            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoints(location)
                        Timber.d("new location: ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    //function that update our location tracking
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationsPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fuesdLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fuesdLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //function to pause the service
    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private var isTimerEnabled = false

    //time from the beginning where the timer started or we resumed the timer
    private var lapTime = 0L

    //total time of our run
    private var timeRun = 0L

    //time when we started the timer
    private var timeStarted = 0L

    private var lastSecondTimeStamp = 0L

    //this will be called always when we start or resume our service
    private fun startTimer() {

        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {

            //as long as we are tracking
            while (isTracking.value!!) {

                //time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                //post the new lap time
                timeRunInMillis.postValue(timeRun + lapTime)

                //last whole second values passed in millisecond => lastSecondTimeStamp + 1000
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000) {

                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }

            timeRun += lapTime
        }
    }
}