package com.softbankrobotics.deep_pepper

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class CallbackExecutor(
    qiContext: QiContext,
    val callback: suspend (params: MutableList<String>) -> Unit
): BaseQiChatExecutor(qiContext) {
    var deferred: Deferred<Unit>? = null

    override fun runWith(params: MutableList<String>) {
        runBlocking {
            deferred = async { callback(params) }
            deferred?.await()
        }
    }
    override fun stop() {
        deferred?.cancel()
    }
}