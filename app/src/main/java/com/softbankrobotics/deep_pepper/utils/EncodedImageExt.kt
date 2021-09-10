package com.softbankrobotics.deep_pepper.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.softbankrobotics.dx.pepperextras.image.toBitmap
import com.softbankrobotics.dx.pepperextras.util.TAG
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

fun EncodedImage.fitToContainer(width: Int, height: Int): EncodedImage {
    val bitmap = toBitmap()
    val scaleRatio = max(width.toDouble() / bitmap.width, height.toDouble() / bitmap.height)
    val scaledWidth = (bitmap.width * scaleRatio).toInt()
    val scaledHeight = (bitmap.height * scaleRatio).toInt()
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap,
        scaledWidth, scaledHeight, false)
    val cropWidthStart = (scaledWidth - width) / 2
    val cropHeightStart = (scaledHeight - height) / 2
    val cropedBitmap = Bitmap.createBitmap(scaledBitmap, cropWidthStart, cropHeightStart, width, height)
    val stream = ByteArrayOutputStream()
    cropedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray: ByteArray = stream.toByteArray()
    return EncodedImage(ByteBuffer.wrap(byteArray))
}


fun EncodedImage.fitToContainerRatio(containerWidth: Int, containerHeight: Int): EncodedImage {
    val bitmap = toBitmap()
    val ratio = max(containerWidth.toDouble() / bitmap.width, containerHeight.toDouble() / bitmap.height)
    val newWidth = min((containerWidth / ratio).toInt(), bitmap.width)
    val newHeight = min((containerHeight / ratio).toInt(),  bitmap.height)
    val cropWidthStart = (bitmap.width - newWidth) / 2
    val cropHeightStart = (bitmap.height - newHeight) / 2
    val cropedBitmap = Bitmap.createBitmap(bitmap, cropWidthStart, cropHeightStart, newWidth, newHeight)
    val stream = ByteArrayOutputStream()
    cropedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray: ByteArray = stream.toByteArray()
    return EncodedImage(ByteBuffer.wrap(byteArray))
}