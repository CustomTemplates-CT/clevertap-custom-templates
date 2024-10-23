package com.clevertap.ctcustomtemplates.ui

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.ctcustomtemplates.databinding.ActivityInAppBinding

class InAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInAppBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        binding = ActivityInAppBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        binding.fands.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("FeedbackAndSurveyInApp")
        }

        binding.scratchcard.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("ScratchcardInApp")
        }

        binding.spinthewheel.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("SpinTheWheelInApp")
        }

        binding.fsgif.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("FullScreenGIFInApp")
        }

        binding.fwcarousel.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("FullScreenCarouselInApp")
        }

        binding.giffooter.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("GifFooterInApp")
        }

        binding.cwtacta.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("CardTextCtaInApp")
        }

        binding.bscarousel.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("BottomSheetCarouselInApp")
        }

        binding.btcornerin.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("BottomCornerInApp")
        }

        binding.copycouponbt.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("CopyCouponInApp")
        }

        binding.ytvideo.setOnClickListener {
            cleverTapDefaultInstance!!.pushEvent("YoutubeVideoInApp")
        }

        if ("Dismiss" == intent.action) {
            var notificationId: Int = intent.getIntExtra("nid", -1)
            if (notificationId != -1) {
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }

        val message = intent.getStringExtra("coupon")
        if (message != null) {
            textCopy(this, message)
        }
    }

    private fun textCopy(context: Context, couponCode: String) {
        try {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", couponCode)
            clipboard.setPrimaryClip(clip)
        } catch (e: java.lang.Exception) {
            Log.e(
                "Exception - ", "PushTemplateRenderer " + e.localizedMessage
            )
        }
    }
}