package com.softbankrobotics.deep_pepper.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.softbankrobotics.deep_pepper.R
import kotlinx.android.synthetic.main.help_screen.*

class HelpScreenFragment(val objectList: List<String>, val background: Bitmap): Fragment(R.layout.help_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spinner: Spinner = view.findViewById(R.id.word_spinner)
        spinner.adapter = ArrayAdapter(view.context, R.layout.layout_spinner_text, objectList).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown)
        }
        spinner.visibility = View.VISIBLE
        backgroundImage.setImageBitmap(background)
    }
}