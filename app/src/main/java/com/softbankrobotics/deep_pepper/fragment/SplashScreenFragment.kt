package com.softbankrobotics.deep_pepper.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.softbankrobotics.deep_pepper.R
import com.softbankrobotics.dx.pepperextras.util.TAG

class SplashScreenFragment: Fragment(R.layout.splash_screen) {

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "---- Splashsceen on RESUME")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "---- Splashsceen on PAUSE")
    }
}
