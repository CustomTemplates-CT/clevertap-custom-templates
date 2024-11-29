package com.clevertap.ct_templates.nd.coachmark

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject

class CoachMarkHelper {

    lateinit var coachMarkSequence: CoachMarkSequence

    fun renderCoachMark(context: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        try {
            coachMarkSequence = CoachMarkSequence(context)
            coachMarkSequence.apply {
                var customKv = unit.optJSONObject("custom_kv")
                if (customKv != null && customKv.has("nd_json")) {
                    val ndJsonString = customKv.optString("nd_json", null)
                    if (!ndJsonString.isNullOrEmpty()) {
                        val parsedNdJson = JSONObject(ndJsonString)
                        Log.d("CoachMarkHelper", "Parsed nd_json: $parsedNdJson")
                        for (key in parsedNdJson.keys()) {
                            customKv.put(key, parsedNdJson.get(key))
                        }
                    }
                }

                Log.d("CoachMarkHelper", "Merged customKv: $customKv")
                val coachMarkCount = customKv?.optInt("nd_coachmarks_count", -1)
                if (coachMarkCount == -1) {
                    throw JSONException("'nd_coachmarks_count' is missing or invalid")
                }

                for (i in 1..coachMarkCount!!) {
                    try {
                        val titleKey = "nd_view${i}_title"
                        val subTitleKey = "nd_view${i}_subtitle"
                        val viewIdKey = "nd_view${i}_id"
                        val viewIdString = customKv.optString(viewIdKey)
                        if (viewIdString.isNullOrEmpty()) {
                            throw JSONException("'$viewIdKey' is missing or empty")
                        }

                        val viewId = context.resources.getIdentifier(
                            viewIdString,
                            "id",
                            context.packageName
                        )
                        if (viewId == 0) {
                            throw JSONException("Invalid view ID for '$viewIdKey': $viewIdString")
                        }

                        val isLastItem = (i == coachMarkCount)
                        addCoachMarkItem(viewId, titleKey, subTitleKey, isLastItem, context, customKv)
                    } catch (e: JSONException) {
                        Log.e(
                            "CoachMarkHelper",
                            "Error processing coach mark item $i: ${e.message}"
                        )
                    }
                }

                // Start the coach mark sequence
                start(context.window?.decorView as ViewGroup)
                setOnFinishCallback {
                    onComplete()
                }
            }
        } catch (e: JSONException) {
            Log.e("CoachMarkHelper", "Error initializing CoachMarkSequence: ${e.message}")
        } catch (e: Exception) {
            Log.e("CoachMarkHelper", "Unexpected error: ${e.message}")
        }
    }

    fun addCoachMarkItem(
        viewId: Int,
        titleKey: String,
        subTitleKey: String,
        isLastItem: Boolean = false,
        context: AppCompatActivity,
        customKv: JSONObject
    ) {
        try {
            coachMarkSequence.addItem(
                targetView = context.findViewById(viewId),
                title = customKv.optString(titleKey, "Default Title"),
                subTitle = customKv.optString(subTitleKey, "Default Subtitle"),
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
                    Color.parseColor(
                        customKv.optString(
                            "nd_skip_button_background_color",
                            "#FFFFFF"
                        )
                    )
                } else {
                    Color.TRANSPARENT
                },
                skipButtonTextColor = if (!isLastItem) {
                    Color.parseColor(customKv.optString("nd_skip_button_text_color", "#000000"))
                } else {
                    Color.TRANSPARENT
                }
            )
        } catch (e: JSONException) {
            Log.e("CoachMarkHelper", "Error adding coach mark item: ${e.message}")
        } catch (e: Exception) {
            Log.e("CoachMarkHelper", "Unexpected error: ${e.message}")
        }
    }
}
