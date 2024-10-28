package com.clevertap.ctcustomtemplates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.nd.tooltips.Tooltip
import com.clevertap.ct_templates.nd.tooltips.TooltipHelper
import com.clevertap.ctcustomtemplates.databinding.ActivityTooltipsBinding
import java.util.ArrayList

class TooltipsActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityTooltipsBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTooltipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.pushEvent("ToolsTipsND")

        cleverTapDefaultInstance?.setDisplayUnitListener(this)
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        println("ToolTips unit: $unit")
        unit.customExtras.forEach { (key, value) ->
            println("$key: $value")
        }

        if (unit.customExtras["nd_id"] == "nd_tooltips") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            TooltipHelper().showTooltips(this@TooltipsActivity, unit.jsonObject){
                CleverTapAPI.getDefaultInstance(this@TooltipsActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}