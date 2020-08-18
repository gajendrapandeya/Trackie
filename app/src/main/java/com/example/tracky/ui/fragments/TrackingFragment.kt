package com.example.tracky.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracky.R
import com.example.tracky.other.Constants
import com.example.tracky.other.Constants.ACTION_PAUSE_SERVICE
import com.example.tracky.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.tracky.other.Constants.MAP_ZOOM
import com.example.tracky.other.Constants.POLYLINE_COLOR
import com.example.tracky.other.Constants.POLYLINE_WIDTH
import com.example.tracky.other.TrackingUtility
import com.example.tracky.services.Polyline
import com.example.tracky.services.TrackingService
import com.example.tracky.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    //actual map object
    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var curTimeInMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)


        btnToggleRun.setOnClickListener {
           toggleRun()
        }

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    //mapView has it's own lifecycle which we have to handle
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    //function to draw line of last two locations
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {

            //second last element
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]

            //last
            val lastLatLng = pathPoints.last().last()

            val polyOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polyOptions)
        }
    }

    //function to draw whole line
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polyOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polyOptions)
        }
    }

    //function that moves the camera to user position whenever there is new position in our pathPoint list
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {

            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    //last coordinate inside our pathPoint list
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        //currently in pause state
        if(!isTracking) {
            btnToggleRun.text = "START"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "STOP"
            btnFinishRun.visibility = View.GONE
        }
    }

    //function to toggle our run
    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //function to subscribe to our liveData objects
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }
}