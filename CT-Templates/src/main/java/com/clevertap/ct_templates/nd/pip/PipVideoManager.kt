package com.clevertap.ct_templates.nd.pip

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.VideoView
import androidx.annotation.RequiresApi
import org.json.JSONObject
import android.content.res.Resources
import android.view.ViewOutlineProvider
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.MotionEvent
import android.util.Log
import androidx.core.net.toUri
import com.clevertap.ct_templates.R

@RequiresApi(Build.VERSION_CODES.O)
class PipVideoManager(
    private val context: Context,
    private val unitJsonObject: JSONObject,
    private val onViewed: (() -> Unit)? = null,
    private val onClicked: (() -> Unit)? = null
) {

    private lateinit var pipView: View
    private lateinit var videoView: VideoView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnClose: ImageButton
    private lateinit var btnExpand: ImageButton
    private lateinit var btnMute: ImageButton

    private var isPlaying = false
    private var isMuted = true
    private var isExpanded = false
    private var lastX = 0f
    private var lastY = 0f

    private var videoWidth = 0
    private var videoHeight = 0
    private var mediaPlayer: MediaPlayer? = null
    private var videoUri: Uri? = null
    private var hasViewedBeenTracked = false

    private val rootView: ViewGroup by lazy {
        (context as? android.app.Activity)
            ?.findViewById(android.R.id.content)!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize() {
        Log.d("PipDebug", "Initializing PipVideoManager...")

        val inflater = LayoutInflater.from(context)
        pipView = inflater.inflate(R.layout.pip_layout, rootView, false)
        rootView.addView(pipView)

        // Find views
        videoView = pipView.findViewById(R.id.video_view)
        btnPlayPause = pipView.findViewById(R.id.btn_play_pause)
        btnClose = pipView.findViewById(R.id.btn_close)
        btnExpand = pipView.findViewById(R.id.btn_expand)
        btnMute = pipView.findViewById(R.id.btn_mute)

        pipView.apply {
            visibility = View.INVISIBLE
            setRoundedCorners(20.dp.toFloat())
        }

        setupListeners()
        makeDraggable(pipView)

        val videoUrl = unitJsonObject.optJSONObject("custom_kv")?.optString("nd_video_url")
        Log.d("PipDebug", "Extracted video URL: $videoUrl")

        if (!videoUrl.isNullOrBlank()) {
            videoUri = videoUrl.toUri()
            playVideo(videoUrl)
        } else {
            Log.e("PipDebug", "Video URL is null or blank")
        }
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            isPlaying = if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                false
            } else {
                videoView.start()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                true
            }
        }

        btnClose.setOnClickListener {
            videoView.stopPlayback()
            videoView.setOnPreparedListener(null)
            videoView.tag = null

            val parent = videoView.parent as ViewGroup
            val index = parent.indexOfChild(videoView)
            parent.removeView(videoView)

            videoView = VideoView(context).apply {
                id = R.id.video_view
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }

            parent.addView(videoView, index)
            pipView.visibility = View.GONE

            onClicked?.invoke()
        }

        btnExpand.setOnClickListener { toggleSize() }
        btnMute.setOnClickListener { muteVideo() }
    }

    fun playVideo(s: String) {
        Log.d("PipDebug", "Playing video from URL: $s")

        pipView.visibility = View.INVISIBLE
        videoView.stopPlayback()

        videoUri = s.toUri()
        videoView.setVideoURI(videoUri)
        videoView.tag = videoUri

        videoView.setOnPreparedListener { mp ->
            Log.d("PipDebug", "Video prepared, starting playback...")

            mp.isLooping = true
            mediaPlayer = mp

            videoWidth = mp.videoWidth
            videoHeight = mp.videoHeight

            saveAspectRatio(videoWidth, videoHeight)
            updatePipViewSize(videoWidth, videoHeight)

            setMute(mp, true)
            isMuted = true

            videoView.seekTo(0)
            videoView.start()
            isPlaying = true
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)

            pipView.alpha = 0f
            pipView.visibility = View.VISIBLE
            pipView.animate().alpha(1f).setDuration(300).start()
            if (!hasViewedBeenTracked) {
                onViewed?.invoke()
                hasViewedBeenTracked = true
            }
        }

        videoView.setOnErrorListener { _, what, extra ->
            Log.e("PipDebug", "Video playback error: what=$what, extra=$extra")
            true
        }
    }

    private fun toggleSize() {
        val params = pipView.layoutParams

        if (isExpanded) {
            updatePipViewSize(videoWidth, videoHeight)
            pipView.x = lastX
            pipView.y = lastY
            pipView.setRoundedCorners(24.dp.toFloat())
        } else {
            lastX = pipView.x
            lastY = pipView.y
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            pipView.layoutParams = params
            pipView.x = 0f
            pipView.y = 0f
            pipView.clearRoundedCorners()
        }

        isExpanded = !isExpanded
    }

    private fun updatePipViewSize(w: Int, h: Int) {
        pipView.layoutParams = pipView.layoutParams.apply {
            width = if (w > h) 300.dp else 160.dp
            height = if (w > h) 180.dp else 280.dp
        }
    }

    private fun muteVideo() {
        isMuted = !isMuted
        mediaPlayer?.let { setMute(it, isMuted) }

        btnMute.setImageResource(
            if (isMuted) R.drawable.ct_volume_off
            else R.drawable.ct_volume_on
        )
    }

    private fun setMute(mp: MediaPlayer, mute: Boolean) {
        mp.setVolume(if (mute) 0f else 1f, if (mute) 0f else 1f)
    }

    private fun makeDraggable(view: View) {
        var dX = 0f
        var dY = 0f

        view.apply {
            bringToFront()
            setOnTouchListener { v, event ->
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
    }

    // Extensions
    private val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun View.setRoundedCorners(radius: Float) {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }

    private fun View.clearRoundedCorners() {
        clipToOutline = false
        outlineProvider = null
    }

    private fun saveAspectRatio(w: Int, h: Int) {
        context.getSharedPreferences("videoAspectRatio", Context.MODE_PRIVATE).edit().apply {
            putInt("width", w)
            putInt("height", h)
            apply()
        }
    }
}
