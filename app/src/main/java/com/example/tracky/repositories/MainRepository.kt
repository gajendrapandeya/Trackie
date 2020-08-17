package com.example.tracky.repositories

import com.example.tracky.db.Run
import com.example.tracky.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
        val runDao: RunDao
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAverageSpeed() = runDao.getAllRunsSortedByAverageSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurnt()

    fun getTotalAverageSpeed() = runDao.getTotalAverageSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurnt()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
}