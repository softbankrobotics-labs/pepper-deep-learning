package com.softbankrobotics.deep_pepper.detector

import android.graphics.RectF
import com.aldebaran.qi.sdk.`object`.image.EncodedImage

data class DetectedObject(val label: String, val boundingBox: RectF, val confidence: Float) {}

abstract class Detector {
    abstract fun processImage(image: EncodedImage): Map<String, MutableList<DetectedObject>>
}