package com.clevertap.ct_templates.nd.coachmark

import android.graphics.Color
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class CoachMarkHelper {

    lateinit var coachMarkSequence: CoachMarkSequence

    fun renderCoachMark(context: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        coachMarkSequence = CoachMarkSequence(context)
        coachMarkSequence.apply {
            val coachMarkCount = unit.getJSONObject("custom_kv").getInt("nd_coachmarks_count")
            for (i in 1..coachMarkCount) {
                val titleKey = "nd_view${i}_title"
                val subTitleKey = "nd_view${i}_subtitle"
                val viewId = context.resources.getIdentifier(
                    unit.getJSONObject("custom_kv").getString("nd_view${i}_id"),
                    "id",
                    context.packageName
                )
                val isLastItem = (i == coachMarkCount)
                addCoachMarkItem(viewId, titleKey, subTitleKey, isLastItem, context, unit)
            }

            start(context.window?.decorView as ViewGroup)
            setOnFinishCallback {
                onComplete()
            }
        }
    }

    fun addCoachMarkItem(
        viewId: Int,
        titleKey: String,
        subTitleKey: String,
        isLastItem: Boolean = false,
        context: AppCompatActivity,
        unit: JSONObject
    ) {
        val customKv = unit.getJSONObject("custom_kv")

        coachMarkSequence.addItem(
            targetView = context.findViewById(viewId),
            title = customKv.getString(titleKey),
            subTitle = customKv.getString(subTitleKey),
            positiveButtonText = if (isLastItem) {
                customKv.optString("nd_final_positive_button_text", "Ready to Explore")
            } else {
                customKv.optString("nd_positive_button_text", "Next")
            },
            skipButtonText = if (isLastItem) null else customKv.optString(
                "nd_skip_button_text",
                "Skip"
            ),
            positiveButtonTextColor = Color.parseColor(
                customKv.optString("nd_positive_button_text_color", "#FFFFFF")
            ),
            positiveButtonBGColor = Color.parseColor(
                customKv.optString("nd_positive_button_background_color", "#E83938")
            ),
            skipButtonBGColor = if (!isLastItem) {
                Color.parseColor(customKv.optString("nd_skip_button_background_color", "#FFFFFF"))
            } else {
                Color.TRANSPARENT
            },
            skipButtonTextColor = if (!isLastItem) {
                Color.parseColor(customKv.optString("nd_skip_button_text_color", "#000000"))
            } else {
                Color.TRANSPARENT
            }
        )
    }
}