package com.clevertap.ctcustomtemplates.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Outline
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.ct_templates.TemplateRenderer
import com.clevertap.ct_templates.nd.NativeDisplayListener
import com.clevertap.ctcustomtemplates.CTApplication
import com.clevertap.ctcustomtemplates.R
import com.clevertap.ctcustomtemplates.databinding.ActivityNativeDisplayBinding
import androidx.core.net.toUri

class NativeDisplayActivity : AppCompatActivity(), NativeDisplayListener, DisplayUnitListener {

    private lateinit var binding: ActivityNativeDisplayBinding
    private lateinit var cleverTapDefaultInstance: CleverTapAPI

    private lateinit var pipView: FrameLayout
    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnClose: ImageButton
    private lateinit var btnExpand: ImageButton
    private lateinit var btnMute: ImageButton
    private lateinit var btnLaunchPip: Button

    private var isExpanded = false
    private var isMuted = false
    private var isPlaying = false

    private lateinit var currentMediaPlayer: MediaPlayer

    private var mediaPlayer: MediaPlayer? = null
    private var videoAspectRatio: Float = 0f

    private var lastX = 0f
    private var lastY = 0f
    private var videoWidth = 0
    private var videoHeight = 0

//            val videoUri = "https://gbct17.github.io/gbimages/landingvid.mp4".toUri()
        val videoUri = "https://videos.pexels.com/video-files/7565438/7565438-hd_1080_1920_25fps.mp4".toUri()
//    val videoUri = "https://karthik-ct.github.io/image-gallery/Videos/relay.mp4".toUri()

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
                pipManagerNew()
                pipView.visibility = View.VISIBLE
                playVideo()
//                TemplateRenderer.getInstance().renderPiP(applicationContext, unit.jsonObject, binding.main)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun pipManagerNew() {
        pipView = findViewById(R.id.pip_view)
        videoView = findViewById(R.id.video_view)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnClose = findViewById(R.id.btn_close)
        btnExpand = findViewById(R.id.btn_expand)
        btnMute = findViewById(R.id.btn_mute)

        pipView.visibility = View.INVISIBLE  // 🛠️ Added this line to fix initial flicker

        setupListeners()
        makeDraggable(pipView)

        pipView.clipToOutline = true
        pipView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = 20.dp.toFloat()
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }

    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                isPlaying = false
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                videoView.start()
                isPlaying = true
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        btnClose.setOnClickListener {
            videoView.stopPlayback()
            videoView.setOnPreparedListener(null)
            videoView.tag = null

            // Remove and re-add the videoView to clear surface/frame
            val parent = videoView.parent as ViewGroup
            val index = parent.indexOfChild(videoView)
            parent.removeView(videoView)

            // Create a new VideoView instance
            val newVideoView = VideoView(this)
            newVideoView.id = R.id.video_view  // keep same ID if used elsewhere

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            newVideoView.layoutParams = layoutParams

            // Add it back in the same place
            parent.addView(newVideoView, index)

            // Re-bind it
            videoView = newVideoView

            pipView.visibility = View.GONE
        }

        btnExpand.setOnClickListener {
            toggleSize()
        }

        btnMute.setOnClickListener {
            muteVideo()
        }
    }

    private fun playVideo() {
        pipView.visibility = View.INVISIBLE // Hide initially to avoid flicker

        // Reset video view
        videoView.stopPlayback()
        videoView.setVideoURI(videoUri)
        videoView.tag = videoUri
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mediaPlayer = mp

            // Get correct dimensions
            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight

            saveAspectRatio(videoWidth, videoHeight)
            updatePipViewSize(videoWidth, videoHeight)

            setMute(mp, true)
            isMuted = true

            // Start playback from beginning
            videoView.seekTo(0)
            videoView.start()
            isPlaying = true
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)

            // Now show pipView (after playback starts)
            pipView.alpha = 0f
            pipView.visibility = View.VISIBLE
            pipView.animate().alpha(1f).setDuration(300).start()
        }

    }

    private fun saveAspectRatio(width: Int, height: Int) {
        // Save the video aspect ratio (width and height) in shared preferences for future use
        val sharedPref = getSharedPreferences("videoAspectRatio", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("width", width)
        editor.putInt("height", height)
        editor.apply()
    }

    private fun updatePipViewSize(videoWidth: Int, videoHeight: Int) {
        val params = pipView.layoutParams

        if (videoWidth > videoHeight) {
            // Landscape
            params.width = 300.dp
            params.height = 180.dp
        } else {
            // Portrait
            params.width = 160.dp
            params.height = 280.dp
        }

        pipView.layoutParams = params
    }

    private fun toggleSize() {
        val params = pipView.layoutParams

        if (isExpanded) {
            // --- UNEXPAND ---
            updatePipViewSize(videoWidth, videoHeight)

            pipView.x = lastX
            pipView.y = lastY

            // ✅ Apply rounded corners
            pipView.clipToOutline = true
            pipView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val cornerRadius = 24.dp.toFloat()
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }

        } else {
            // --- EXPAND ---
            lastX = pipView.x
            lastY = pipView.y

            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            pipView.layoutParams = params

            pipView.x = 0f
            pipView.y = 0f

            // ❌ Remove rounded corners
            pipView.clipToOutline = false
            pipView.outlineProvider = null
        }

        isExpanded = !isExpanded
    }


    private fun muteVideo() {
        isMuted = !isMuted
        mediaPlayer?.let { setMute(it, isMuted) }

        if (isMuted) {
            btnMute.setImageResource(com.clevertap.android.sdk.R.drawable.ct_volume_off)
        } else {
            btnMute.setImageResource(com.clevertap.android.sdk.R.drawable.ct_volume_on)
        }
    }

    private fun setMute(mp: MediaPlayer, mute: Boolean) {
        val volume = if (mute) 0f else 1f
        mp.setVolume(volume, volume)
    }

    private fun makeDraggable(view: View) {
        var dX = 0f
        var dY = 0f
        view.bringToFront()
        view.requestLayout()
        view.invalidate()
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }
            true
        }
    }

    // Extension
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}