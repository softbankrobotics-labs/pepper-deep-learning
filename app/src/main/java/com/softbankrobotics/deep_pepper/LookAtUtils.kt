package com.softbankrobotics.deep_pepper

import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.actuation.LookAtMovementPolicy
import com.aldebaran.qi.sdk.`object`.geometry.Vector3
import com.aldebaran.qi.sdk.builder.LookAtBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.softbankrobotics.dx.pepperextras.actuation.ExtraLookAt
import com.softbankrobotics.dx.pepperextras.actuation.ExtraLookAtBuilder
import com.softbankrobotics.dx.pepperextras.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay


object LookAtUtils {

    val GAZE_FRAME_Z = 1.1165693310431193

    suspend fun createLookAtFrame(qiContext: QiContext, x: Double, y: Double, z: Double): Frame {
        val transform = TransformBuilder.create().fromTranslation(Vector3(x, y, z))
        val robotFrame = qiContext.actuationAsync.await().async().robotFrame().await()
        return robotFrame.async().makeAttachedFrame(transform).await().async().frame().await()
    }

    suspend fun lookAt(qiContext: QiContext, x: Double, y: Double, z: Double): Future<Void> {
        Log.i(TAG, "Lookat")
        val lookAtFrame = createLookAtFrame(qiContext, x, y, z)
        return LookAtBuilder.with(qiContext).withFrame(lookAtFrame).buildAsync().await().apply {
            policy = LookAtMovementPolicy.HEAD_ONLY
        }.async().run()
    }

    suspend fun lookLeft(qiContext: QiContext): Future<Void> {
        return lookAt(qiContext, 1.0, 1.0, GAZE_FRAME_Z)
    }

    suspend fun lookRight(qiContext: QiContext): Future<Void> {
        return lookAt(qiContext, 1.0, -1.0, GAZE_FRAME_Z)
    }

    private enum class LOOKING { LEFT, FRONT, RIGHT}

    fun lookAround(qiContext: QiContext): Future<Unit> = SingleThread.GlobalScope.asyncFuture {
        Log.i(TAG, "Lookaruond")
        val lookAtFrame = qiContext.mapping.async().makeFreeFrame().await()
        val robotFrame = qiContext.actuationAsync.await().async().robotFrame().await()
        val transformLeft = TransformBuilder.create().fromTranslation(Vector3(1.0, 1.0, GAZE_FRAME_Z))
        val transformRight = TransformBuilder.create().fromTranslation(Vector3(1.0, -1.0, GAZE_FRAME_Z))
        val transformFront = TransformBuilder.create().fromTranslation(Vector3(1.0, 0.0, GAZE_FRAME_Z))
        lookAtFrame.async().update(robotFrame, transformLeft, 0).await()
        var looking = LOOKING.LEFT
        var extraLookAtFuture: Future<Void>? = null
        val targetFrame = lookAtFrame.async().frame().await()
        val lookAt = ExtraLookAtBuilder.with(qiContext)
            .withFrame(targetFrame)
            .withTerminationPolicy(ExtraLookAt.TerminationPolicy.RUN_FOREVER)
            .buildAsync().await()

        lookAt.policy = LookAtMovementPolicy.HEAD_ONLY

        lookAt.async().addOnStatusChangedListener(object : ExtraLookAt.OnStatusChangedListener {
            override fun onStatusChanged(status: ExtraLookAt.LookAtStatus) {

                if (status == ExtraLookAt.LookAtStatus.LOOKING_AT
                    || status == ExtraLookAt.LookAtStatus.NOT_LOOKING_AT_AND_NOT_MOVING_ANYMORE) {
                    when (looking) {
                        LOOKING.LEFT -> {
                            looking = LOOKING.FRONT
                            SingleThread.GlobalScope.async {
                                delay(2000)
                                lookAtFrame.async().update(robotFrame, transformFront, 0)
                                lookAt.async().resetStatus()
                            }
                        }
                        LOOKING.FRONT -> {
                            looking = LOOKING.RIGHT
                            SingleThread.GlobalScope.async {
                                delay(2000)
                                lookAtFrame.async().update(robotFrame, transformRight, 0)
                                lookAt.async().resetStatus()
                            }
                        }
                        LOOKING.RIGHT -> {
                            SingleThread.GlobalScope.async {
                                delay(2000)
                                extraLookAtFuture?.requestCancellation()
                            }
                        }
                    }

                }
            }
        }).await()
        extraLookAtFuture = lookAt.async().run()
        extraLookAtFuture.awaitOrNull()
        Unit
    }
}

