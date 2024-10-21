package com.clevertap.ctcustomtemplates

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle

import android.widget.CompoundButton

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.clevertap.android.pushtemplates.TemplateRenderer
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.ctcustomtemplates.databinding.ActivityMainBinding
import com.clevertap.ctcustomtemplates.databinding.ActivityRestaurantBinding
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Objects
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener,
    PushPermissionResponseListener {
    lateinit var binding: ActivityMainBinding
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    var defaultFirebaseAnalytics: FirebaseAnalytics? = null
    var eventButton: AppCompatButton? = null
    var profilePushButton: AppCompatButton? = null
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var inAppButton: AppCompatButton? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        cleverTapDefaultInstance = (this.application as CTApplication).getCTInstance()

        binding.navigatetoinapp.setOnClickListener {
            startActivity(Intent(applicationContext,InAppActivity::class.java))
        }
        binding.navigatecoachmark.setOnClickListener {
            startActivity(Intent(applicationContext,RestaurantActivity::class.java))
        }

        binding.pushButton.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("CopyCouponPush")
        }
        binding.pushgif.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("GifPush")
        }
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
            Toast.makeText(this, R.string.channel_not_created, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeCleverTapSDK() {

        try {

            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
            cleverTapDefaultInstance!!.enableDeviceNetworkInfoReporting(true);
            cleverTapDefaultInstance!!.enablePersonalization()
            cleverTapDefaultInstance!!.registerPushPermissionNotificationResponseListener(this)
            TemplateRenderer.debugLevel = 3

        } catch (e: Exception) {
            Toast.makeText(this, R.string.sdk_not_initialized, Toast.LENGTH_SHORT).show()
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

}