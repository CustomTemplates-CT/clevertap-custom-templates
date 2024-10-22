package com.clevertap.ctcustomtemplates.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.ctcustomtemplates.CTApplication
import com.clevertap.ctcustomtemplates.R
import com.clevertap.ctcustomtemplates.databinding.ActivityMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Objects

class MainActivity : AppCompatActivity(), PushPermissionResponseListener {
    private lateinit var binding: ActivityMainBinding
    private var defaultFirebaseAnalytics: FirebaseAnalytics? = null
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        setFirebaseInstance()
        startHandler()
        cleverTapDefaultInstance = (this.application as CTApplication).getCTInstance()

        binding.openInAppActivity.setOnClickListener {
            startActivity(Intent(applicationContext, InAppActivity::class.java))
        }

        binding.openPushActivity.setOnClickListener {
            startActivity(Intent(applicationContext, PushTemplatesActivity::class.java))
        }

        binding.openNativeDisplayActivity.setOnClickListener {
            startActivity(Intent(applicationContext, NativeDisplayActivity::class.java))
        }

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
                cleverTapDefaultInstance!!.registerPushPermissionNotificationResponseListener(this)
                val builder = CTLocalInApp.builder().setInAppType(CTLocalInApp.InAppType.ALERT)
                    .setTitleText("Get Notified").setMessageText("Enable Notification permission")
                    .followDeviceOrientation(true).setPositiveBtnText("Allow")
                    .setNegativeBtnText("Cancel").setFallbackToSettings(true).build()
                cleverTapDefaultInstance!!.promptPushPrimer(builder)
            }
        }
    }

    private fun setFirebaseInstance() {
        try {
            defaultFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            defaultFirebaseAnalytics!!.setUserProperty(
                "ct_objectId",
                Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this)?.cleverTapID)
            )
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Firebase Initialisation failed", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPushNotifications() {
        try {
            if (cleverTapDefaultInstance != null) {

            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.channel_not_created, Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPushPermissionResponse(accepted: Boolean) {
        if (accepted) {
            setupPushNotifications()
        }
    }


}