package com.clevertap.ctcustomtemplates.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.TemplateRenderer
import com.clevertap.ct_templates.nd.NativeDisplayListener
import com.clevertap.ctcustomtemplates.CTApplication
import com.clevertap.ctcustomtemplates.R
import com.clevertap.ctcustomtemplates.databinding.ActivityNativeDisplayBinding

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

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            if (unit.customExtras["nd_id"].equals("nd_pip_video")) {
                TemplateRenderer.getInstance().showNativeDisplay(
                    R.id.pip_fragment, supportFragmentManager, unit.jsonObject, this
                )
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