package com.softbankrobotics.deep_pepper.utils

import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.softbankrobotics.deep_pepper.detector.DetectedObject


private data class RectP(var rectF: RectF)


object ObjectColor {
    private val colors = listOf(
        Color.parseColor("#f89b3c"),
        Color.parseColor("#f8e075"),
        Color.parseColor("#a4d758"),
        Color.parseColor("#45cfe1"),
        Color.parseColor("#183cbd"),
        Color.parseColor("#9541d1"),
        Color.parseColor("#ed3679"),
    )
    private var index = 0

    private val objectToColor = mutableMapOf<String, Int>()

    private fun assignColor(obj: String): Int {
        if (index >= colors.size) index = 0
        val color = colors[index]
        index += 1
        objectToColor[obj] = color
        return color
    }

    fun get(obj: String): Int { return objectToColor[obj] ?: assignColor(obj) }
}

fun Bitmap.drawObjectBoxes(objects: List<DetectedObject>, font: Typeface) {
    val canvas = Canvas(this)
    //val verdanaFont = Typeface.createFromAsset(assetManager, "font/verdana.ttf")
    val textPaint = Paint().apply {
        strokeWidth = 2f;
        textSize = 25f;
        style = Paint.Style.STROKE;
        typeface = font
    }

    val boxPaint = Paint()
    boxPaint.alpha = 200;
    boxPaint.style = Paint.Style.STROKE;
    boxPaint.strokeWidth = 2 * getWidth().toFloat() / 800;
    val offset = 20f
    val rects = objects.map { RectP(it.boundingBox) }.toMutableList()
    val rectsBig = objects.map {
        RectF(
            it.boundingBox.left - offset,
            it.boundingBox.top - offset,
            it.boundingBox.right + offset,
            it.boundingBox.bottom + offset
        )
    }.toMutableList()

    for (i in 0..rects.size-1) {
        for (j in i+1..rects.size-1) {
            if (RectF.intersects(rectsBig[i], rectsBig[j])) {
                rects[i].rectF.union(rects[j].rectF)
                rects[j] = rects[i]
                rectsBig[i].union(rectsBig[j])
                rectsBig[j] = rectsBig[i]
            }
        }
    }
    val label = objects.first().label
    val minWidth = label.length * 16

    rects.forEach {
        val r = it.rectF

        // Make sure bounding box is big enough to contain label text
        val minRight = r.left + minWidth
        if (r.right < minRight)
            r.right = minRight

        boxPaint.setColor(ObjectColor.get(label));
        boxPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(RectF(r.left, r.top, r.right, r.top + 35), boxPaint);
        boxPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(r, boxPaint);
        textPaint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawText(
            label,
            r.left + 3,
            r.top + 25, textPaint
        )
    }
}

fun Bitmap.blur(appContext: Context): Bitmap {
    return try {
        val rsScript = RenderScript.create(appContext)
        val alloc = Allocation.createFromBitmap(rsScript, this)
        val blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
        blur.setRadius(21f)
        blur.setInput(alloc)
        val result = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val outAlloc = Allocation.createFromBitmap(rsScript, result)
        blur.forEach(outAlloc)
        outAlloc.copyTo(result)
        rsScript.destroy()
        result
    } catch (e: Exception) {
        this
    }
}

fun Bitmap.darken(): Bitmap {
    val canvas = Canvas(this)
    val p = Paint(Color.RED)
    val filter: ColorFilter = LightingColorFilter(-0x808081, 0x00000000) // darken
    p.colorFilter = filter
    canvas.drawBitmap(this, Matrix(), p)
    return this
}
