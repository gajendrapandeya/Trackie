package com.example.tracky

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

//we need to tell our app that it need to use dagger-hilt as dependency injection tool
//So for this this class is required
@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}