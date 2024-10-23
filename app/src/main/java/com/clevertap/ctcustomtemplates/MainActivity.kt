package com.clevertap.ctcustomtemplates

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.ct_templates.TemplateRenderer
import com.clevertap.ct_templates.nd.NativeDisplayListener
import com.clevertap.ctcustomtemplates.databinding.ActivityMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Objects


class MainActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener,
    PushPermissionResponseListener, DisplayUnitListener, NativeDisplayListener {
    lateinit var binding: ActivityMainBinding
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    var defaultFirebaseAnalytics: FirebaseAnalytics? = null
    var eventButton: AppCompatButton? = null
    var profilePushButton: AppCompatButton? = null
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var inAppButton: AppCompatButton? = null
    var videoViewPIP: VideoView? = null
    private var dX = 0f
    var dY:kotlin.Float = 0f
    private var lastAction = 0
    var relpop: RelativeLayout? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        cleverTapDefaultInstance = (this.application as CTApplication).getCTInstance()
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

        initializeCleverTapSDK()
        setFirebaseInstance()

        startHandler()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startHandler() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(200)
            checkAndRequestPushPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    private fun checkAndRequestPushPermission() {

        if (null != cleverTapDefaultInstance) {
            if (cleverTapDefaultInstance!!.isPushPermissionGranted) {
                setupPushNotifications()
            } else {
                val builder = CTLocalInApp.builder()
                    .setInAppType(CTLocalInApp.InAppType.ALERT)
                    .setTitleText("Get Notified")
                    .setMessageText("Enable Notification permission")
                    .followDeviceOrientation(true)
                    .setPositiveBtnText("Allow")
                    .setNegativeBtnText("Cancel")
                    .setFallbackToSettings(true)
                    .build()
                cleverTapDefaultInstance!!.promptPushPrimer(builder)
            }
        }

    }

    private fun setFirebaseInstance() {
        try {
            defaultFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            defaultFirebaseAnalytics!!.setUserProperty("ct_objectId",
                Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this))?.cleverTapID
            );
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Firebase Initialisation failed", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPushNotifications() {
        try {
            if (cleverTapDefaultInstance != null) {
                CleverTapAPI.createNotificationChannelGroup(
                    applicationContext,
                    "1234",
                    "CleverTapPush"
                )
                CleverTapAPI.createNotificationChannel(
                    applicationContext,
                    "CTCustom",
                    "CT-Push",
                    "Test-Notifications",
                    NotificationManager.IMPORTANCE_MAX,
                    "1234",
                    true
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Notification Channel not created", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeCleverTapSDK() {

        try {

            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
            cleverTapDefaultInstance!!.enableDeviceNetworkInfoReporting(true);
            cleverTapDefaultInstance!!.enablePersonalization()
            cleverTapDefaultInstance!!.registerPushPermissionNotificationResponseListener(this)

        } catch (e: Exception) {
            Toast.makeText(this, "SDK not initialised", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView!!.id) {
            //R.id.setOptout -> cleverTapDefaultInstance!!.setOptOut(isChecked)
            //R.id.offline -> cleverTapDefaultInstance!!.setOffline(isChecked)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPushPermissionResponse(accepted: Boolean) {
        if (accepted) {
            setupPushNotifications()
        }
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        Log.d("NativeDisplay", "payload$units")
        for (i in 0 until units!!.size) {
            val unit = units[i]
            if (unit.customExtras["nd_id"].equals("nd_pip_video")) {
                TemplateRenderer.getInstance().showNativeDisplay(
                    R.id.pip_fragment, supportFragmentManager, unit.jsonObject, this
                )
            } else if (unit.customExtras["nd_id"].equals("nd_custom_button")) {
                TemplateRenderer.getInstance().animateButton(
                    applicationContext,
                    binding.root as ViewGroup?, unit.jsonObject, this
                )
            }
        }
    }

    override fun onSuccess(id: String?) {
        //Template rendered successfully.
        cleverTapDefaultInstance!!.pushDisplayUnitViewedEventForID(id)
    }

    override fun onFailure(id: String?) {

    }

    override fun onClick(resId: Int, id: String?, deepLink: String?) {
        cleverTapDefaultInstance!!.pushDisplayUnitClickedEventForID(id)
    }

    private fun initiatePIP() {
        cleverTapDefaultInstance!!.pushEvent("ShowPIPND")
    }


    private fun playVideo(videoUrl: String) {
        val videoUri = Uri.parse(videoUrl)
        videoViewPIP!!.setVideoURI(videoUri)
        videoViewPIP!!.start()
        videoViewPIP!!.resume()
    }

}