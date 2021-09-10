package com.softbankrobotics.deep_pepper

import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.softbankrobotics.dx.pepperextras.util.TAG
import com.softbankrobotics.dx.pepperextras.util.await
import com.softbankrobotics.dx.pepperextras.util.awaitOrNull
import java.util.*

class Dialog(
    val qiContext: QiContext,
    val bookmarkActions: HashMap<String, () -> Unit>,
    val executors: HashMap<String, CallbackExecutor>,
    val objectList: List<String>
) {

    private var qiChatbot: QiChatbot? = null
    private var mainTopic: Topic? = null
    public var visibleObjectVariable: QiChatVariable? = null
    public var continuousDescribeObjects: QiChatVariable? = null
    public var savedObjectVariable: QiChatVariable? = null
    private var chat: Chat? = null

    public suspend fun load() {
        val defaultTopic = TopicBuilder.with(qiContext).withResource(R.raw.dialog).buildAsync().await()
        val chatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(defaultTopic)
            .buildAsync().await()

        visibleObjectVariable = chatbot.variable("visible_objects")
        continuousDescribeObjects = chatbot.variable("continuous_describe_objects")
        savedObjectVariable = chatbot.variable("saved_objects")

        val objectsListConcept = chatbot.dynamicConcept("object_list")
        objectList.forEach { obj ->
            objectsListConcept?.async()?.addPhrases(
                Collections.singletonList(Phrase(obj))
            )?.await()
        }

        chatbot.addOnBookmarkReachedListener { bookmarkActions[it.name]?.invoke() }
        chatbot.executors = executors as HashMap<String, QiChatExecutor>

        chat = ChatBuilder.with(qiContext).withChatbot(chatbot).buildAsync().await()
            .apply { addOnStartedListener {
                val startBookmark = defaultTopic.bookmarks["START"]
                chatbot.async().goToBookmark(
                    startBookmark,
                    AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE
                )
            } }

        mainTopic = defaultTopic
        qiChatbot = chatbot
    }

    public suspend fun run() {
        chat?.async()?.run()?.awaitOrNull()
    }

    private fun goToBookmark(bookmark: String) {
        mainTopic?.bookmarks?.get(bookmark)?.let {
            qiChatbot?.async()?.goToBookmark(
                it, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE
            )
        }
    }

    public fun goToHelpBookmark() { goToBookmark("HELP") }
    public fun goToMainDialogBookmark() { goToBookmark("MAIN_DIALOG") }
    public fun goToObjectFoundBookmark() { goToBookmark("OBJECT_FOUND") }
    public fun goToObjectNotFoundBookmark() { goToBookmark("OBJECT_NOT_FOUND") }
    public fun goToOneShotDescribeBookmark() { goToBookmark("ONE_SHOT_DESCRIBE") }
    public fun goToContinuousDescribeBookmark() { goToBookmark("CONTINUOUS_DESCRIBE") }
    public fun goToLookAroundBookmark() { goToBookmark("LOOK_AROUND") }
}