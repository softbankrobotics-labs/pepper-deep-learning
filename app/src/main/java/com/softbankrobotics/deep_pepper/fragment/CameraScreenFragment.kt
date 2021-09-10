package com.softbankrobotics.deep_pepper.fragment

import android.util.Log
import androidx.fragment.app.Fragment
import com.softbankrobotics.deep_pepper.R
import com.softbankrobotics.dx.pepperextras.util.TAG

class CameraScreenFragment: Fragment(R.layout.display_camera_and_objects) {
    override fun onResume() {
        super.onResume()
        Log.i(TAG, "---- CameraScreenFragment on RESUME")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "---- CameraScreenFragment on PAUSE")
    }
}