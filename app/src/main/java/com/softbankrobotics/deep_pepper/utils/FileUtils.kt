package com.softbankrobotics.facemaskdetection.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Upload file to storage and return a path.
fun assetToAppStoragePath(file: String, context: Context): String {
    val assetManager: AssetManager = context.getAssets()
    var inputStream: BufferedInputStream? = null
    try {

        // Read data from assets.
        inputStream = BufferedInputStream(assetManager.open(file))
        val data = ByteArray(inputStream.available())
        inputStream.read(data)
        inputStream.close()
        // Create copy file in storage.
        val outFile = File(context.getFilesDir(), file.substringAfterLast("/"))
        val os = FileOutputStream(outFile)
        os.write(data)
        os.close()

        // Return a path to file which may be read in common way.
        return outFile.getAbsolutePath()
    } catch (ex: IOException) {
        Log.i("assetToAppStoragePath", "Failed to upload a file: $ex")
    }
    return ""
}

fun readFromAssetStorage(file: String, context: Context): String {
    val assetManager: AssetManager = context.getAssets()
    var inputStream: BufferedInputStream? = null
    try {

        // Read data from assets.
        inputStream = BufferedInputStream(assetManager.open(file))
        val data = ByteArray(inputStream.available())
        inputStream.read(data)
        inputStream.close()
        return data.toString()
    } catch (ex: IOException) {
        Log.i("readFromAssetStorage", "Failed to read a file: $ex")
    }
    return ""
}
