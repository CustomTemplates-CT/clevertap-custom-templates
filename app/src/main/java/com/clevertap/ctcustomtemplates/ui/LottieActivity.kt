package com.clevertap.ctcustomtemplates.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.databinding.YearlinelayoutBinding
import com.clevertap.ct_templates.nd.spotlights.SpotlightHelper
import com.clevertap.ct_templates.nd.yearline.YearLine
import com.clevertap.ctcustomtemplates.R
import com.clevertap.ctcustomtemplates.databinding.ActivityLottieBinding
import com.clevertap.ctcustomtemplates.databinding.ActivitySpotlightsBinding


class LottieActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityLottieBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
        cleverTapDefaultInstance?.pushEvent("YearlineND")
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        if (unit.customExtras["nd_id"] == "nd_yearline") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            YearLine().showYearLine(this@LottieActivity, unit.jsonObject, binding.root.findViewById(R.id.container)) {
                CleverTapAPI.getDefaultInstance(this@LottieActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}