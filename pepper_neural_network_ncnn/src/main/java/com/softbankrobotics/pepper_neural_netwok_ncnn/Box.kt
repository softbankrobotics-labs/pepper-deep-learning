package com.softbankrobotics.pepper_neural_network_ncnn

import android.graphics.Color
import android.graphics.RectF
import kotlin.random.Random

class Box(
    var x0: Float,
    var y0: Float,
    var x1: Float,
    var y1: Float,
    val label: Int,
    val score: Float
) {
    val rect: RectF
        get() = RectF(x0, y0, x1, y1)

    val color: Int
        get() {
            val random = Random(label)
            return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
}
