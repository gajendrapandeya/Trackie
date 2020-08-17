package com.example.tracky.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    //this will insert new run into db
    //OnConflictStrategy.Replace mean when we want to insert a run that already exist in database
    //old run will be replaced by new function
    //suspend mean this function will be executed inside coroutine
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    //this will not be a suspend function
    //because whenever we want to get something from our db and we want to return a liveData object
    //so that we can simply observe on, then this doesn't work in coroutine
    //if we want to sort your runs by date then we want to have the latest run on top of this list
    //so sort it by descending
    //these all functions will be for run fragment
    @Query("SELECT * FROM running_table ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurnt DESC")
    fun getAllRunsSortedByCaloriesBurnt(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY averageSpeedInKmH DESC")
    fun getAllRunsSortedByAverageSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    //this functions will be for statistics fragment
    //this query will sum  all the available timeInMillis from our db table
    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurnt) FROM running_table")
    fun getTotalCaloriesBurnt(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    //this query will average  all the available averageSpeedInKmH from our db table
    @Query("SELECT avg(averageSpeedInKmH) FROM running_table")
    fun getTotalAverageSpeed(): LiveData<Float>
}