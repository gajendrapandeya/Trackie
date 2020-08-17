package com.example.tracky.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.tracky.R
import com.example.tracky.ui.viewmodels.StastisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_stastistics) {
    private val viewModel: StastisticsViewModel by viewModels()
}