package com.example.tracky.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(

        //preview image
        var image: Bitmap? = null,

        //it describes when our run was started
        var timeStamp: Long = 0L,

        //average speed travelled
        var averageSpeedInKmH: Float = 0F,

        //how far we ran
        var distanceInMeters: Int = 0,

        //how long was our run was
        var timeInMillis: Long = 0L,

        //calories burnt
        var caloriesBurnt: Int = 0
) {

    //we didn't made this as constructor param because we want to create Run object without providing primary key
    //room will handle that
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}