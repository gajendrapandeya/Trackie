package com.example.tracky.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.tracky.repositories.MainRepository
import javax.inject.Inject

class StastisticsViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
): ViewModel() {

        val totalTimeRun = mainRepository.getTotalTimeInMillis()
        val totalDistance = mainRepository.getTotalDistance()
        val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
        val totalAvgSpeed = mainRepository.getTotalAverageSpeed()

        val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
}