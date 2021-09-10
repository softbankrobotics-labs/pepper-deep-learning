package com.softbankrobotics.pepper_neural_network_ncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

class Yolo {

    companion object {
        init {
            System.loadLibrary("yolo")
        }

        external fun init(manager: AssetManager, param: String, bin: String)
        external fun detect(bitmap: Bitmap, threshold: Double, nmsThreshold: Double): Array<Box>
    }
}