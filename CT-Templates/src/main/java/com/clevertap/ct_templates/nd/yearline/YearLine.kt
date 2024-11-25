package com.clevertap.ct_templates.nd.yearline

import android.annotation.SuppressLint
import android.graphics.HardwareRenderer.FrameRenderRequest
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.ct_templates.R
import org.json.JSONObject

class YearLine {
    private var viewflipper : ViewFlipper? = null
    private var rootView : FrameLayout? = null
    private var overlay : View? = null

    fun showYearLine(activity: AppCompatActivity, unit: JSONObject, rootView : FrameLayout, onComplete: () -> Unit) {

        try{
            this.rootView = rootView
            overlay = activity.layoutInflater.inflate(R.layout.yearlinelayout, rootView)
            viewflipper = overlay!!.findViewById(R.id.viewflipper)
            viewflipper!!.removeAllViews()
            setTouchListener(activity)
            setupCards(unit,activity)

        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun setupCards(unit:JSONObject, activity: AppCompatActivity) {
        try{
            val cardCount = (unit.getJSONObject("custom_kv").getString("nd_cardCount")).toInt()
            for (i in 1..cardCount) {
                val titleSize = unit.getJSONObject("custom_kv").getString("nd_titleSize")
                val descSize = unit.getJSONObject("custom_kv").getString("nd_textSize")

                val animUrl = unit.getJSONObject("custom_kv").getString("nd_anim$i")
                val cardTitle = unit.getJSONObject("custom_kv").getString("nd_title$i")
                val cardDesc = unit.getJSONObject("custom_kv").getString("nd_desc$i")

                val yearlineCard = activity.layoutInflater.inflate(R.layout.yearline_card, null)
                var textTitle = yearlineCard.findViewById<TextView>(R.id.cardTitle)
                var textDesc = yearlineCard.findViewById<TextView>(R.id.cardDesc)
                var  anim = yearlineCard!!.findViewById<LottieAnimationView>(R.id.lottieAnimationView)

                anim.setAnimationFromUrl(animUrl)
                textTitle!!.text = cardTitle
                textDesc!!.text = cardDesc
                textDesc.textSize = descSize.toFloat()
                textTitle.textSize = titleSize.toFloat()

                viewflipper!!.addView(yearlineCard)
            }

            rootView!!.addView(overlay)

        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setTouchListener(activity: AppCompatActivity) {
        try {
            val gestureDetector = GestureDetector(activity, object : SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null || e2 == null) return false
                    if (e1.x < e2.x) {
                        viewflipper?.showPrevious()
                    } else if (e1.x > e2.x) {
                        viewflipper?.showNext()
                    }
                    return true
                }
            })

            viewflipper?.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}