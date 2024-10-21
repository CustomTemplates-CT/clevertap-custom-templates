package com.clevertap.ctcustomtemplates

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.ctcustomtemplates.databinding.ActivityInAppBinding
import com.clevertap.ctcustomtemplates.databinding.ActivityRestaurantBinding

class InAppActivity : AppCompatActivity() {

    lateinit var binding: ActivityInAppBinding
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


    }
}