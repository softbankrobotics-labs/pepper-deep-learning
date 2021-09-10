package com.softbankrobotics.deep_pepper

import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.softbankrobotics.deep_pepper.LookAtUtils.lookLeft
import com.softbankrobotics.deep_pepper.LookAtUtils.lookRight
import com.softbankrobotics.dx.pepperextras.util.TAG

/*
 *   During continuous description, look left and right when robot has nothing to describe anymore
 *   to search for new objects.
 */
class LookAtSequence(val qiContext: QiContext) {

    enum class State { INITIAL, LEFT, FRONT, RIGHT }

    private var startTime: Long = 0
    private var lookAtFuture: Future<Void>? = null
    private var state = State.INITIAL

    init {
        reset()
    }

    suspend fun next() {
        Log.i(TAG, "next")
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        if (elapsedTime > 12000) {
            reset()
        } else if (state == State.INITIAL && elapsedTime > 3000) {
            lookAtFuture?.cancel(true)
            lookAtFuture = lookLeft(qiContext)
            state = State.LEFT
        } else if (state == State.LEFT && elapsedTime > 6000) {
            lookAtFuture?.cancel(true)
            lookAtFuture = null
            state = State.FRONT
        } else if (state == State.FRONT && elapsedTime > 9000) {
            lookAtFuture?.cancel(true)
            lookAtFuture = lookRight(qiContext)
            state = State.RIGHT
        }
    }

    fun reset() {
        Log.i(TAG, "reset")
        state = State.INITIAL
        startTime = System.currentTimeMillis()
        lookAtFuture?.cancel(true)
        lookAtFuture = null
    }
}