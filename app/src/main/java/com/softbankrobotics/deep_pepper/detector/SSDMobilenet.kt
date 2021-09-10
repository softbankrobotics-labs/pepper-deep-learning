package com.softbankrobotics.deep_pepper.detector

import android.content.Context
import android.graphics.*
import androidx.tracing.trace
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.softbankrobotics.dx.pepperextras.image.toBitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.round

class SSDMobilenet(val context: Context,
                   val assetFileName: String,
                   thresholdScore: Float,
                   val translations: Map<String, String>): Detector() {

    private val objectDetector: ObjectDetector
    private val tfimage = TensorImage()

    init {
        val options = ObjectDetector.ObjectDetectorOptions.builder().setNumThreads(4)
            .setDisplayNamesLocale("fr")
            .setScoreThreshold(thresholdScore).build()
        objectDetector = ObjectDetector.createFromBufferAndOptions(loadModelFile(), options)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(assetFileName)
        val fileInputStream = FileInputStream(assetFileDescriptor.getFileDescriptor())
        val fileChannel = fileInputStream.getChannel()
        val startoffset = assetFileDescriptor.getStartOffset()
        val declaredLength = assetFileDescriptor.getDeclaredLength()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength)
    }


    override fun processImage(image: EncodedImage): Map<String, MutableList<DetectedObject>> {
        val picture = image.toBitmap()
        val rescaled = Bitmap.createScaledBitmap(picture, 300, 300, false)
        tfimage.load(rescaled)
        val detections = objectDetector.detect(tfimage)
        val result = mutableMapOf<String, MutableList<DetectedObject>>()
        for (detection in detections) {
            val score = detection.categories[0].score
            val confidence = round(score * 100) / 100
            val label =
                translations[detection.categories[0].label] ?: detection.categories[0].label
            val bbox = RectF(
                detection.boundingBox.left / 300 * picture.width,
                detection.boundingBox.top / 300 * picture.height,
                detection.boundingBox.right / 300 * picture.width,
                detection.boundingBox.bottom / 300 * picture.height
            )

            if (!result.containsKey(label))
                result[label] = mutableListOf()
            result[label]?.add(DetectedObject(label, bbox, confidence))
        }
        return result.toMap()
    }
}