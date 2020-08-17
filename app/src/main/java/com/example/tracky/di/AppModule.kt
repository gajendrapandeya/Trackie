package com.example.tracky.di

import android.content.Context
import androidx.room.Room
import com.example.tracky.db.RunningDatabase
import com.example.tracky.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

//We are telling our app that it should install this module inside of our ApplicationComponent Class
//that is all those dependencies will be created inside onCreate() of BaseApplication by Hilt
//and that will exist throughout our app lifecycle
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
            @ApplicationContext app: Context
    ) = Room.databaseBuilder(
            app,
            RunningDatabase::class.java,
            RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(
            db: RunningDatabase
    ) = db.getRunDao()
}