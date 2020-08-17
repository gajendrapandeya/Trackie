package com.example.tracky.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
        entities = [Run::class],
        version = 1
)

@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    //function that returns an instance of runDao
    abstract fun getRunDao(): RunDao
}