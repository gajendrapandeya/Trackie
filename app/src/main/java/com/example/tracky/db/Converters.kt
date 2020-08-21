package com.example.tracky.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

//since room db take care of primitives only but we have Bitmap as well in our Run constructor
//so we need a way to convert the bitmap into the language that room underStands as well as
//we need a way to convert back into HumanUnderStandAble form
//this class basically serve as a converter for us
class Converters {

    //since both of our function for room, so we need to annotate them with TypeConverter

    //converting bitmap into the form room understand i.e ByteArray
    //this function will take a bitmap and convert that it into raw_bites that is stored in room as byteArray
    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        //outputStream is needed to convert bitmap into byteArray
        val outputStream = ByteArrayOutputStream()

        //format-> png, full quality, these compressed byte will be saved in outputStream
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    //converting byteArray into Bitmap again i.e. humanUnderstandable
    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}