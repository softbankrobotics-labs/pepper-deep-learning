package com.softbankrobotics.deep_pepper

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.actuation.LookAtMovementPolicy
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.`object`.image.EncodedImage
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.aldebaran.qi.sdk.builder.LookAtBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.deep_pepper.LookAtUtils.lookAround
import com.softbankrobotics.deep_pepper.detector.DetectedObject
import com.softbankrobotics.deep_pepper.detector.cocoTransFR
import com.softbankrobotics.deep_pepper.fragment.CameraScreenFragment
import com.softbankrobotics.deep_pepper.fragment.HelpScreenFragment
import com.softbankrobotics.deep_pepper.fragment.SplashScreenFragment
import com.softbankrobotics.deep_pepper.utils.blur
import com.softbankrobotics.deep_pepper.utils.darken
import com.softbankrobotics.deep_pepper.utils.drawObjectBoxes
import com.softbankrobotics.deep_pepper.utils.fitToContainerRatio
import com.softbankrobotics.dx.pepperextras.image.toBitmap
import com.softbankrobotics.dx.pepperextras.util.*
import kotlinx.android.synthetic.main.display_camera_and_objects.*
import kotlinx.android.synthetic.main.help_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : RobotActivity(), RobotLifecycleCallbacks, TakePictures.TakePicturesListener {

    private var holder: Holder? = null
    private var dialog: Dialog? = null
    private var detectObjects: DetectObjects? = null
    private var takePictures: TakePictures? = null
    private var takePicturesFuture: Future<Unit>? = null
    private var action: Action? = null
    private var dialogFuture: Future<Unit>? = null

    private val cameraScreenFragment = CameraScreenFragment()

    private var lookForObjectFuture: Future<Unit>? = null
    private var lastImage: Bitmap = Bitmap.createBitmap(1200, 900, Bitmap.Config.ARGB_8888);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, SplashScreenFragment())
                .commit()
        }
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        QiSDK.unregister(this)
    }

    override fun onRobotFocusGained(qiContext: QiContext) {

        holder  = HolderBuilder.with(qiContext)
            .withAutonomousAbilities(AutonomousAbilitiesType.BASIC_AWARENESS)
            .build()
        holder?.hold()
        Log.i(TAG, "FOCUS OK")

        val language = Locale.getDefault().getLanguage()
        val translations = if (language == "fr") cocoTransFR else mapOf()
        val objectListFile = if (language == "fr") "word_list_fr.txt" else "word_list_en.txt"
        val objectList = assets.open(objectListFile).bufferedReader().use { it.readText() }.lines()


        val bookmarkActions = hashMapOf<String, () -> Unit>()
        val executors = hashMapOf<String, CallbackExecutor>()

        val dlg = Dialog(qiContext, bookmarkActions, executors, objectList)
        dialog = dlg

        val oneShotDescribeAction = OneShotDescribe(dlg)
        val continuousDescribeAction = ContinuousDescribe(dlg, qiContext)
        val searchForObjectAction = SearchForObject(qiContext)
        val lookAroundForObjectsAction = LookAroundForObjects(dlg, language)

        bookmarkActions["CONTINUOUS_DESCRIPTION"] = {
            continuousDescribeAction.reset()
            action = continuousDescribeAction
        }
        bookmarkActions["MAIN_DIALOG"] = {
            enableMainDialog()
            action = oneShotDescribeAction
        }

        bookmarkActions["HELP"] = {
            Log.i(TAG, "HELP")
            enableHelpMenu(objectList)
        }
        bookmarkActions["continuous_describe_unlock"] = {
            continuousDescribeAction.continuousDescribeLock = false
        }
        var lookAtObjectFoundFuture: Future<Void>? = null
        bookmarkActions["lookAtObjectFound"] = {
            searchForObjectAction.objectFoundLocation?.let {
                lookAtObjectFoundFuture = LookAtBuilder.with(qiContext).withFrame(it).buildAsync()
                    .andThenApply {
                        it.policy = LookAtMovementPolicy.HEAD_ONLY; it
                    }
                    .andThenCompose { it.async().run() }
            }
        }
        bookmarkActions["stopLookAtObjectFound"] = {
            lookAtObjectFoundFuture?.requestCancellation()
        }

        executors["lookAround"] = CallbackExecutor(qiContext) {
            Log.i(TAG, "LOOKAROUND executor start")
            lookAroundForObjectsAction.reset()
            action = lookAroundForObjectsAction
            lookAround(qiContext).await()
            action = oneShotDescribeAction
            Log.i(TAG, "LOOKAROUND executor stop")
        }
        executors["searchForObject"] = CallbackExecutor(qiContext) {
            Log.i(TAG, "SEARCH FOR OBJECT executor start")
            lookForObjectFuture = lookAround(qiContext)
            searchForObjectAction.reset(it[0]) {
                lookForObjectFuture?.requestCancellation()
            }
            action = searchForObjectAction
            lookForObjectFuture?.awaitOrNull()
            if (searchForObjectAction.objectFoundLocation != null) {
                dialog?.goToObjectFoundBookmark()
            } else {
                dialog?.goToObjectNotFoundBookmark()
            }
            action = oneShotDescribeAction
            Log.i(TAG, "SEARCH FOR OBJECT executor stop")
        }
        executors["sleep"] = CallbackExecutor(qiContext) {
            val amount = it[0].toLong()
            delay(amount)
        }


        detectObjects = DetectObjects(
            qiContext, "lite-model_ssd_mobilenet_v1_1_metadata_2.tflite", translations)
        takePictures = TakePictures(qiContext, this, this)

        dialogFuture = SingleThread.GlobalScope.asyncFuture {
            dlg.load()
            enableMainDialog()
            action = oneShotDescribeAction
            dlg.run()
        }
    }

    override fun onPause() {
        super.onPause()
        supportFragmentManager.popBackStack()
    }

    override fun onRobotFocusLost() {
        holder?.release()
        holder = null
        dialogFuture?.requestCancellation()
        takePicturesFuture?.requestCancellation()
        takePicturesFuture = null
    }

    override fun onRobotFocusRefused(reason: String?) {
        Log.i(TAG, "FOCUS REFUSED")
    }

    fun enableMainDialog() {
        if (takePicturesFuture == null) {
            takePicturesFuture = takePictures?.run()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, cameraScreenFragment)
                .commit()
            Log.i(TAG, "Start object detection")
        }
    }

    fun enableHelpMenu(objectList: List<String>) {
        takePicturesFuture?.requestCancellation()
        takePicturesFuture = null
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, HelpScreenFragment(objectList, lastImage.blur(applicationContext).darken()))
            .commit()
    }

    fun helpButtonClicked(view: View) {
        SingleThread.GlobalScope.launch { dialog?.goToHelpBookmark() }
    }

    fun quitHelpButtonClicked(view: View) {
        SingleThread.GlobalScope.launch { dialog?.goToMainDialogBookmark() }
    }

    override fun onPicture(picture: EncodedImage, timestamp: Long) {
        val fittedPicture = picture.fitToContainerRatio(camera.measuredWidth, camera.measuredHeight)
        detectObjects?.run(fittedPicture)?.andThenConsume { detectedObjects ->
            SingleThread.GlobalScope.asyncFuture {
                action?.run(detectedObjects, timestamp)
            }
            displayObjectsAndPicture(fittedPicture, detectedObjects)
        }
    }

    fun displayObjectsAndPicture(image: EncodedImage, detectedObjects: Map<String, MutableList<DetectedObject>>) {
        val bmpImage = image.toBitmap()
        runOnUiThread { lastImage = bmpImage }
        val bitmap = bmpImage.copy(Bitmap.Config.ARGB_8888, true)

        detectedObjects.forEach { obj -> bitmap.drawObjectBoxes(obj.value, ResourcesCompat.getFont(applicationContext, R.font.verdana)!!) }
        runOnUiThread {
            cameraScreenFragment.view?.findViewById<ImageView>(R.id.camera)?.setImageBitmap(bitmap)
        }
    }
}
