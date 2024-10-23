package com.clevertap.ctcustomtemplates

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class CTApplication : Application(), CTPushAmpListener, CTPushNotificationListener {
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    val TAG: String = String.format(
        "%s.%s", "CLEVERTAP", CTApplication::class.java.name
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        CleverTapAPI.createNotificationChannel(
            applicationContext,
            "CTCustom",
            "CT-Push",
            "CT-Push",
            NotificationManager.IMPORTANCE_MAX, true
        )

        setCTInstance()
        //setIdentifierForRTUT()
        val cleverTapAPI = CleverTapAPI.getDefaultInstance(applicationContext)
        checkNotNull(cleverTapAPI)
        cleverTapAPI.ctPushAmpListener = this
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler?)

        ActivityLifecycleCallback.register(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener<String?> { task ->
            if (!task.isSuccessful) {
                Log.w(
                    TAG, "Fetching FCM registration token failed", task.exception
                )
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            cleverTapDefaultInstance!!.pushFcmRegistrationId(token, true)
            // Log and toast
            Log.d("----- FCM token -----", token!!)
        })
        setActivityCallbacks()

    }

    override fun onPushAmpPayloadReceived(extras: Bundle?) {
        CleverTapAPI.createNotification(applicationContext, extras)
    }

    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
    }

    private fun setActivityCallbacks() {
        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                CleverTapAPI.setAppForeground(true)
                try {
                    CleverTapAPI.getDefaultInstance(this@CTApplication)!!
                        .pushNotificationClickedEvent(activity.intent.extras)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        dismissNotification(
                            activity.intent, this@CTApplication
                        )
                    }
                } catch (t: Throwable) {
                    // Ignore
                }
                try {
                    val intent = activity.intent
                    val data = intent.data
                    CleverTapAPI.getDefaultInstance(this@CTApplication)!!.pushDeepLink(data)
                } catch (t: Throwable) {
                    // Ignore
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                try {
                    CleverTapAPI.onActivityResumed(activity)
                } catch (t: Throwable) {
                    // Ignore
                }
            }

            override fun onActivityPaused(activity: Activity) {
                try {
                    CleverTapAPI.onActivityPaused()
                } catch (t: Throwable) {
                    // Ignore
                }
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    fun dismissNotification(intent: Intent, applicationContext: Context) {
        val extras = intent.extras
        if (extras != null) {
            val actionId = extras.getString("actionId")
            if (actionId != null) {
                val autoCancel = extras.getBoolean("autoCancel", true)
                val notificationId = extras.getInt("notificationId", -1)
                if (autoCancel && notificationId > -1) {
                    val notifyMgr =
                        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notifyMgr.cancel(notificationId)
                }
            }
        }
    }

    fun getCTInstance(): CleverTapAPI {
        return cleverTapDefaultInstance!!
    }

    fun setCTInstance() {
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        this.cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(this)
    }
}