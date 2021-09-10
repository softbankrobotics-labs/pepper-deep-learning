package com.softbankrobotics.deep_pepper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.tracing.trace
import androidx.tracing.traceAsync
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.aldebaran.qi.sdk.builder.TakePictureBuilder
import com.softbankrobotics.dx.pepperextras.util.SingleThread
import com.softbankrobotics.dx.pepperextras.util.asyncFuture
import com.softbankrobotics.dx.pepperextras.util.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class TakePictures(val qiContext: QiContext, val context: Context, val listener: TakePicturesListener) {

    interface TakePicturesListener {
        fun onPicture(picture: EncodedImage, timestamp: Long)
    }

    public fun run(): Future<Unit> = SingleThread.GlobalScope.asyncFuture {
/*
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.salon)
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        val image = EncodedImage(ByteBuffer.wrap(byteArray))
        val timestamp = 0L
        while (isActive) {
            listener.onPicture(image, timestamp)
            delay(1000)
        }


 */
        val takePicture = TakePictureBuilder.with(qiContext).buildAsync().await()
        var cookie = 0
        while (this.isActive) {
            traceAsync("TakePictures.run.internal_look", cookie++) {
                val pictureHandler = takePicture.async().run().await()
                val picture = pictureHandler.image.async().value.await()
                val timestamp = pictureHandler.time
                listener.onPicture(picture, timestamp)
            }
        }
    }
}