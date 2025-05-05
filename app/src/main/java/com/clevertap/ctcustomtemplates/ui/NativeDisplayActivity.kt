package com.clevertap.ctcustomtemplates.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.TemplateRenderer
import com.clevertap.ct_templates.nd.NativeDisplayListener
import com.clevertap.ctcustomtemplates.CTApplication
import com.clevertap.ctcustomtemplates.databinding.ActivityNativeDisplayBinding
import com.clevertap.ct_templates.nd.pip.PipVideoManager

class NativeDisplayActivity : AppCompatActivity(), NativeDisplayListener, DisplayUnitListener {

    private lateinit var binding: ActivityNativeDisplayBinding
    private lateinit var cleverTapDefaultInstance: CleverTapAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeDisplayBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)
        cleverTapDefaultInstance = (this.application as CTApplication).getCTInstance()
        cleverTapDefaultInstance.setDisplayUnitListener(this)

        cleverTapDefaultInstance.pushEvent("ND Stories")
        
        binding.openCoachMarkActivity.setOnClickListener {
            startActivity(Intent(applicationContext, RestaurantActivity::class.java))
        }

        binding.openTooltipsActivity.setOnClickListener {
            startActivity(Intent(applicationContext, TooltipsActivity::class.java))
        }

        binding.openSpotlightsActivity.setOnClickListener {
            startActivity(Intent(applicationContext, SpotlightsActivity::class.java))
        }

        binding.pipNativeDisplay.setOnClickListener {
            cleverTapDefaultInstance.pushEvent("ShowPIPND")
        }
    }

    override fun onSuccess(id: String?) {
        cleverTapDefaultInstance.pushDisplayUnitViewedEventForID(id)
    }

    override fun onFailure(id: String?) {
        //TODO: handle failure
    }

    override fun onClick(resId: Int, id: String?, deepLink: String?) {
        cleverTapDefaultInstance.pushDisplayUnitClickedEventForID(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            if (unit.customExtras["nd_id"].equals("nd_pip_video")) {
                PipVideoManager(
                    context = this,
                    unitJsonObject = unit.jsonObject,
                    onViewed = {
                        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
                    },
                    onClicked = {
                        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID)
                    }
                ).initialize()
            } else if (unit.customExtras["nd_id"].equals("nd_custom_button")) {
                TemplateRenderer.getInstance().animateButton(
                    applicationContext, binding.root as ViewGroup?, unit.jsonObject, this
                )
            } else if (unit.customExtras["nd_id"].equals("nd_stories")) {
                binding.recyclerViewStory.adapter = TemplateRenderer.getInstance().displayStories(
                    this, unit.jsonObject, true
                )
                binding.recyclerViewStory.adapter?.notifyDataSetChanged()
            }
        }
    }
}