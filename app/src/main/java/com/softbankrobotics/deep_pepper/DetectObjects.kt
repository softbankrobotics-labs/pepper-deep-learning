package com.softbankrobotics.deep_pepper

import android.content.Context
import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.softbankrobotics.deep_pepper.detector.DetectedObject
import com.softbankrobotics.deep_pepper.detector.Detector
import com.softbankrobotics.deep_pepper.detector.SSDMobilenet
import com.softbankrobotics.dx.pepperextras.util.TAG
import com.softbankrobotics.dx.pepperextras.util.asyncFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors


class DetectObjects(
    context: Context,
    modelFile: String,
    translations: Map<String, String>,
) {
    val Dispatcher = Executors.newSingleThreadExecutor {
        Thread(it, "RecognizeObject")
    }.asCoroutineDispatcher()

    private val objectDetector: Detector

    init {
        objectDetector = SSDMobilenet(context, modelFile, 0.5f, translations)
    }

    fun run(picture: EncodedImage): Future<Map<String, MutableList<DetectedObject>>> = CoroutineScope(Dispatcher).asyncFuture {
        val start = System.currentTimeMillis()
        val detectedObjects = objectDetector.processImage(picture)
        Log.d(TAG, "Inference done in ${System.currentTimeMillis() - start}ms")
        detectedObjects
    }
}
