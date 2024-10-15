package com.clevertap.ctcustomtemplates

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.clevertap.ct_templates.TemplateRenderer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.data.apply {
            try {
                if (size > 0) {
                    val extras = Bundle()
                    for ((key, value) in this) {
                        extras.putString(key, value)
//                        if (key == "wzrk_pid"){
//                            extras.putString(key, "test")
//                        }
                    }

                    if (extras.containsKey("nm")) {
                        // Raise the event
                    }
                    val info = CleverTapAPI.getNotificationInfo(extras)
                    if (info.fromCleverTap) {
                        if (extras.containsKey("sticky")) {
//                            sendBroadcast( Intent("MyAction"));

//                            showPIP()
                        } else if (extras.getString("pt_type").equals("custom")) {
                            TemplateRenderer.getInstance().showPushNotification(
                                applicationContext,
                                extras,
                                object :
                                    PushNotificationListener,
                                    com.clevertap.ct_templates.pn.PushNotificationListener {
                                    override fun onPushRendered() {
                                        CleverTapAPI.getDefaultInstance(applicationContext)!!
                                            .pushNotificationViewedEvent(extras) // to track push impression.
                                    }

                                    override fun onPushFailed() {
                                        CTFcmMessageHandler().createNotification(
                                            applicationContext,
                                            message
                                        )
                                    }
                                })
                        } else {
                            CTFcmMessageHandler()
                                .createNotification(applicationContext, message)
                        }
                    } else {
                        // not from CleverTap handle yourself or pass to another provider
                    }
                }
            } catch (t: Throwable) {
                Log.d("MYFCMLIST", "Error parsing FCM message", t)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //PushTemplateHandler.getCleverTapDefaultInstance()?.pushFcmRegistrationId(token, true)
    }
}