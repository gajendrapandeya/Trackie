package com.example.tracky.other

import android.content.Context
import com.example.tracky.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId){

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if(e == null) {
            return
        }

        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calender.time)

        val averageSpeed = "${run.averageSpeedInKmH}Km/h"
        tvAvgSpeed.text = averageSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}Km"
        tvDistance.text = distanceInKm

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurnt = "${run.caloriesBurnt}Kcal"
        tvCaloriesBurned.text = caloriesBurnt
    }
}