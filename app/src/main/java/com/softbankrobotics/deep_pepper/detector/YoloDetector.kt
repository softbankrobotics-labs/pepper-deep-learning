package com.softbankrobotics.deep_pepper.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.softbankrobotics.dx.pepperextras.image.toBitmap
import com.softbankrobotics.dx.pepperextras.util.TAG
import com.softbankrobotics.pepper_neural_network_ncnn.Yolo
import kotlin.math.round
/*
class YoloDetector(context: Context, paramFileName: String, binFilename: String): Detector() {

    override var visibleObjects = hashMapOf<String, Int>()
    override var objectsLocation: HashMap<String, MutableList<PointF>>
        get() = TODO("Not yet implemented")
        set(value) {}
    private val threshold = 0.3
    private val nmsThreshold = 0.7
    private val labels = arrayOf("Pen", "Mobile phone")

    init {
        Yolo.init(context.assets, paramFileName,binFilename)
    }

    override fun processImage(image: EncodedImage): Bitmap {

        val picture = image.toBitmap()
        val rescaled = Bitmap.createScaledBitmap(picture, 640, 480, false)

        val result = Yolo.detect(rescaled, threshold, nmsThreshold)
        val mutableBitmap = rescaled.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val boxPaint = Paint()
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth().toFloat() / 800);
        boxPaint.setTextSize(40 * mutableBitmap.getWidth().toFloat() / 800);
        for (box in result) {
            if (box.score > 0.5) {
                boxPaint.setColor(box.color);
                boxPaint.setStyle(Paint.Style.FILL);
                val confidence = round(box.score * 100) / 100
                canvas.drawText(
                    "${confidence}: ${labels[box.label]}",
                    box.x0 + 3,
                    box.y0 + 40 * mutableBitmap.getWidth() / 1000, boxPaint);
                boxPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(box.rect, boxPaint);
                Log.i(TAG, box.rect.toString())
            }
        }
        return mutableBitmap
    }
}


 */