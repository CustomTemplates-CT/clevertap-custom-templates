package com.clevertap.ctcustomtemplates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.nd.spotlights.SpotlightHelper
import com.clevertap.ctcustomtemplates.databinding.ActivitySpotlightsBinding

class SpotlightsActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivitySpotlightsBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotlightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

        cleverTapDefaultInstance?.pushEvent("SpotlightsND")
    }

    override fun onDisplayUnitsLoaded(units: java.util.ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        if (unit.customExtras["nd_id"] == "nd_spotlight") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            SpotlightHelper().showSpotlight(this@SpotlightsActivity, unit.jsonObject) {
                CleverTapAPI.getDefaultInstance(this@SpotlightsActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }

}