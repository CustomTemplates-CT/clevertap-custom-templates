package com.clevertap.ctcustomtemplates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.ctcustomtemplates.CTApplication
import com.clevertap.ctcustomtemplates.databinding.ActivityPushTemplatesBinding

class PushTemplatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPushTemplatesBinding
    private lateinit var cleverTapDefaultInstance: CleverTapAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushTemplatesBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        cleverTapDefaultInstance = (this.application as CTApplication).getCTInstance()

        binding.copyCodePush.setOnClickListener {
            cleverTapDefaultInstance.pushEvent("CopyCouponPush")
        }

        binding.gifPush.setOnClickListener {
            cleverTapDefaultInstance.pushEvent("GifPush")
        }
    }
}