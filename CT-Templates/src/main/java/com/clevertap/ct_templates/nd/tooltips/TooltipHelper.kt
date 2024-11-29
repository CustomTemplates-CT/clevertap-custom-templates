package com.clevertap.ct_templates.nd.tooltips

import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.ct_templates.R
import org.json.JSONException
import org.json.JSONObject

class TooltipHelper {

    fun showTooltips(anyActivity: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        try {
            val customKv = unit.optJSONObject("custom_kv")?.apply {
                if (has("nd_json")) {
                    val ndJsonString = optString("nd_json", null)
                    if (!ndJsonString.isNullOrEmpty()) {
                        val parsedNdJson = JSONObject(ndJsonString)
                        Log.d("TooltipHelper", "Parsed nd_json: $parsedNdJson")
                        for (key in parsedNdJson.keys()) {
                            put(key, parsedNdJson.get(key))
                        }
                    }
                }
            } ?: throw JSONException("Missing 'custom_kv' in unit JSONObject")

            Log.d("TooltipHelper", "Merged customKv: $customKv")

            val tooltipCount = customKv.optInt("nd_tooltips_count", -1)
            if (tooltipCount == -1) {
                throw JSONException("'nd_tooltips_count' is missing or invalid")
            }

            val textColor = customKv.optString("nd_text_color", "#FFFFFF")
            val backgroundColor = customKv.optString("nd_background_color", "#000000")

            fun showNextTooltip(index: Int) {
                if (index < tooltipCount) {
                    try {
                        val viewIdName = customKv.optString("nd_view${index + 1}_id")
                        if (viewIdName.isNullOrEmpty()) {
                            throw JSONException("'nd_view${index + 1}_id' is missing or empty")
                        }

                        val viewId = anyActivity.resources.getIdentifier(viewIdName, "id", anyActivity.packageName)
                        if (viewId == 0) {
                            throw JSONException("Invalid view ID for 'nd_view${index + 1}_id': $viewIdName")
                        }

                        val text = customKv.optString("nd_view${index + 1}_tooltip", "Default Tooltip Text")
                        val gravity = getGravity(index, customKv)

                        Tooltip.Builder(anyActivity)
                            .anchor(anyActivity.findViewById(viewId), 0, 0, false)
                            .text(text)
                            .tooltipTextColor(Color.parseColor(textColor))
                            .tooltipBackgroundColor(backgroundColor)
                            .styleId(R.style.ToolTipAltStyle)
                            .maxWidth(anyActivity.resources.displayMetrics.widthPixels / 2)
                            .arrow(true)
                            .closePolicy(getClosePolicy())
                            .showDuration(10000)
                            .overlay(true)
                            .create()
                            .doOnHidden { showNextTooltip(index + 1) }
                            .show(anyActivity.findViewById(viewId), gravity, true)

                    } catch (e: JSONException) {
                        Log.e("TooltipHelper", "Error processing tooltip item $index: ${e.message}")
                    } catch (e: Exception) {
                        Log.e("TooltipHelper", "Unexpected error processing tooltip item $index: ${e.message}")
                    }
                }
            }

            showNextTooltip(0)
            onComplete()

        } catch (e: JSONException) {
            Log.e("TooltipHelper", "Error initializing TooltipHelper: ${e.message}")
        } catch (e: Exception) {
            Log.e("TooltipHelper", "Unexpected error: ${e.message}")
        }
    }

    private fun getGravity(index: Int, customKv: JSONObject): Tooltip.Gravity {
        return when (customKv.optString("nd_view${index + 1}_tooltip_gravity", "TOP").uppercase()) {
            "RIGHT" -> Tooltip.Gravity.RIGHT
            "LEFT" -> Tooltip.Gravity.LEFT
            "BOTTOM" -> Tooltip.Gravity.BOTTOM
            "TOP" -> Tooltip.Gravity.TOP
            else -> Tooltip.Gravity.TOP
        }
    }

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()
        builder.inside(true)
        builder.outside(true)
        builder.consume(true)
        return builder.build()
    }
}
