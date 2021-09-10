package com.softbankrobotics.deep_pepper

import android.graphics.PointF
import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.geometry.Quaternion
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.aldebaran.qi.sdk.`object`.geometry.Vector3
import com.softbankrobotics.deep_pepper.detector.DetectedObject
import com.softbankrobotics.dx.pepperextras.actuation.makeDetachedFrame
import com.softbankrobotics.dx.pepperextras.geometry.times
import com.softbankrobotics.dx.pepperextras.util.TAG
import com.softbankrobotics.dx.pepperextras.util.await


interface Action {
    suspend fun run(detectedObjects: Map<String, MutableList<DetectedObject>>, timestamp: Long)
}

class OneShotDescribe(private val dialog: Dialog): Action {
    override suspend fun run(detectedObjects: Map<String, MutableList<DetectedObject>>, timestamp: Long) {
        val objectListAsString = detectedObjects.map { "${it.value.size} ${it.key}" }.joinToString()
        dialog.visibleObjectVariable?.value = objectListAsString
    }
}

class ContinuousDescribe(private val dialog: Dialog, qiContext: QiContext): Action {
    var continuousDescribeLock = false
    private val describedObjectTime = mutableMapOf<String, Long>()
    private var lastDescribedTime: Long = System.currentTimeMillis()
    private var lookAtSequence = LookAtSequence(qiContext)

    fun reset() {
        continuousDescribeLock = false
        describedObjectTime.clear()
        lastDescribedTime = System.currentTimeMillis()
        lookAtSequence.reset()
    }

    override suspend fun run(detectedObjects: Map<String, MutableList<DetectedObject>>, timestamp: Long) {
        if (!continuousDescribeLock) {
            Log.i(TAG, "continuousDescribe ENTER ${detectedObjects.size}")
            val currentTime = System.currentTimeMillis()
            val objectsToDescribe = mutableMapOf<String, Int>()
            detectedObjects.forEach { objEntry ->
                val obj = objEntry.key
                val objectAlreadyDescribed = describedObjectTime.get(obj)
                    ?.let { currentTime - it < 60000 } ?: false
                if (!objectAlreadyDescribed) {
                    objectsToDescribe[obj] = objEntry.value.size
                    describedObjectTime[obj] = currentTime
                }
            }
            Log.i(
                TAG,
                "continuousDescribe ${objectsToDescribe.isNotEmpty()} $objectsToDescribe ${objectsToDescribe.size}"
            )

            if (objectsToDescribe.isNotEmpty()) {
                dialog.continuousDescribeObjects?.value = objectsToDescribe.map {
                    "${it.value} ${it.key}"
                }.joinToString()
                continuousDescribeLock = true
                lastDescribedTime = currentTime
                lookAtSequence.reset()
            } else {
                lookAtSequence.next()
            }
        }
    }
}

class LookAroundForObjects(private val dialog: Dialog, val language: String): Action {
    private val savedObjectMap = mutableMapOf<String, Int>()
    fun reset() {
        Log.i(TAG, "LookAroundForObjects reset")
        savedObjectMap.clear()
    }

    override suspend fun run(detectedObjects: Map<String, MutableList<DetectedObject>>, timestamp: Long) {
        Log.i(TAG, "LookAroundForObjects run start")
        detectedObjects.forEach {
            savedObjectMap[it.key] =
                savedObjectMap[it.key]?.plus(it.value.size) ?: it.value.size
        }
        dialog.savedObjectVariable?.value = savedObjectMap.map {
            if (it.value > 1) {
                if (language == "fr") "plusieurs ${it.key}" else "several ${it.key}"
            } else { "1 ${it.key}" }
        }.joinToString()
        Log.i(TAG, "LookAroundForObjects run stop")
    }
}

class SearchForObject(private val qiContext: QiContext): Action {
    private var objectToSearchFor: String = ""
    private var onObjectFound: ((location: Frame) -> Unit)? = null
    var objectFoundLocation: Frame? = null

    fun reset(obj: String, callback: (location: Frame) -> Unit) {
        objectToSearchFor = obj
        onObjectFound = callback
        objectFoundLocation = null
    }

    override suspend fun run(detectedObjects: Map<String, MutableList<DetectedObject>>, timestamp: Long) {
        objectToSearchFor.let {
            if (detectedObjects.containsKey(it)) {
                val location = detectedObjects[it]!!.first().boundingBox.let {
                    PointF(it.centerX(), it.centerY())
                }
                val locationFrame = computeObjectFrame(location, timestamp, qiContext)
                objectFoundLocation = locationFrame
                onObjectFound?.invoke(locationFrame)
            }
        }
    }

    val GAZE_TO_HEAD_CAMERA_TRANSFORM = Transform(
        Quaternion(0.0, 0.0, 0.0, 1.0),
        Vector3(0.020309998728599843, 0.0, 0.04393999093233303)
    )

    private suspend fun computeObjectFrame(pixelPosition: PointF, timestamp: Long, qiContext: QiContext): Frame {
        // We don't have the object real depth, as we only have access to 2D camera. So we use 1.0
        // for z (like if all objects where 1 meter away).
        val cameraToObjectTranslation = Vector3(
            1.0,
            -(pixelPosition.x - 657.406192) / 1213.91824,
            -(pixelPosition.y - 488.498999) / 1213.91824,

            )
        val cameraToObjectTransform =
            Transform(Quaternion(0.0, 0.0, 0.0, 1.0), cameraToObjectTranslation)
        val gazeToObjectTransform = GAZE_TO_HEAD_CAMERA_TRANSFORM * cameraToObjectTransform
        val gazeFrame = qiContext.actuationAsync.await().async().gazeFrame().await()
        val objectFrame = qiContext.mapping.async().makeDetachedFrame(gazeFrame, gazeToObjectTransform, timestamp).await()
        return objectFrame
    }
}
