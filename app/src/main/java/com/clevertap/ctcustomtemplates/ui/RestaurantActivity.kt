package com.clevertap.ctcustomtemplates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.nd.coachmark.CoachMarkHelper
import com.clevertap.ctcustomtemplates.databinding.ActivityRestaurantBinding
import java.util.ArrayList

class RestaurantActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityRestaurantBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

//        cleverTapDefaultInstance?.pushEvent("CoachmarksND")
        cleverTapDefaultInstance?.pushEvent("coachmarks_nd")

    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        if (unit.customExtras["nd_id"] == "nd_coachmarks") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            CoachMarkHelper().renderCoachMark(this, unit.jsonObject){
                 CleverTapAPI.getDefaultInstance(this@RestaurantActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}