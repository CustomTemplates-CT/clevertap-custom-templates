package com.clevertap.ct_templates.nd.yearline

import android.graphics.PointF
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.ct_templates.nd.spotlights.OnTargetListener
import com.clevertap.ct_templates.nd.spotlights.effect.Effect
import com.clevertap.ct_templates.nd.spotlights.shape.Shape

class AnimatedCard(
    val desc: String,
    val animation: LottieAnimationView,
) {

    class Builder{
        private var description : String? = null;
        private var animation : LottieAnimationView? = null;

    }


}