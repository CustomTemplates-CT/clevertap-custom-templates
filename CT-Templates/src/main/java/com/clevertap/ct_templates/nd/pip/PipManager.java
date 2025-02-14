package com.clevertap.ct_templates.nd.pip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;

import com.clevertap.ct_templates.R;

public class PipManager {
    private final Context context;
    private RelativeLayout pipRelative;
    private VideoView videoView;
    private TextView closeButton, muteUnmuteButton, fullscreenButton;
    private boolean isMuted = false;
    private boolean isExpanded = false;
    private float dX, dY;

    public PipManager(Context context) {
        this.context = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void createPiPLayout(ViewGroup containerView) {
        // Create the parent RelativeLayout
        pipRelative = new RelativeLayout(context);
        pipRelative.setId(View.generateViewId());
        pipRelative.setLayoutParams(new RelativeLayout.LayoutParams(
                dpToPx(320),
                dpToPx(180)
        ));
        pipRelative.setPadding(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
        pipRelative.setVisibility(View.VISIBLE);

        // Enable dragging
        pipRelative.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;
            }
            return true;
        });

        // Create the VideoView
        videoView = new VideoView(context);
        videoView.setId(View.generateViewId());
        RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                dpToPx(320),
                dpToPx(180)
        );
        videoView.setLayoutParams(videoParams);
        videoView.setVisibility(View.VISIBLE);

        videoView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                // Set corner radius to 16dp
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 20f); // 16f for corner radius in pixels
            }
        });
        videoView.setClipToOutline(true);

        // Add VideoView to the RelativeLayout
        pipRelative.addView(videoView);

        // Create the TextView for the close button
        closeButton = new TextView(context);
        closeButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams closeParams = new RelativeLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeButton.setLayoutParams(closeParams);

        Drawable closeDrawable = ContextCompat.getDrawable(context, R.drawable.cross3);
        closeButton.setBackground(closeDrawable);
        closeButton.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
            }
            pipRelative.setVisibility(View.GONE); // Hide the PiP layout
        });

        // Add close button to the RelativeLayout
        pipRelative.addView(closeButton);

        // Create the mute/unmute button
        muteUnmuteButton = new TextView(context);
        muteUnmuteButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams muteParams = new RelativeLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );
        muteParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        muteParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        muteUnmuteButton.setLayoutParams(muteParams);

        Drawable muteDrawable = ContextCompat.getDrawable(context, R.drawable.mute); // Replace with your mute icon
        muteUnmuteButton.setBackground(muteDrawable);
        muteUnmuteButton.setOnClickListener(v -> toggleMute());

        // Add mute/unmute button to the RelativeLayout
        pipRelative.addView(muteUnmuteButton);

        // Create the fullscreen button
        fullscreenButton = new TextView(context);
        fullscreenButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams fullscreenParams = new RelativeLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );
        fullscreenParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        fullscreenParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        fullscreenButton.setLayoutParams(fullscreenParams);

//        Drawable fullscreenDrawable = ContextCompat.getDrawable(context, R.drawable.expan_icon); // Replace with your fullscreen icon
//        fullscreenButton.setBackground(fullscreenDrawable);
//        fullscreenButton.setOnClickListener(v -> toggleFullscreen());

        // Add fullscreen button to the RelativeLayout
//        pipRelative.addView(fullscreenButton);


        // Inside the createPiPLayout method, after adding fullscreenButton:

// Create the Play/Pause toggle button
        TextView playPauseButton = new TextView(context);
        playPauseButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams playPauseParams = new RelativeLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
        );
        playPauseParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        playPauseParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playPauseButton.setLayoutParams(playPauseParams);

        Drawable playDrawable = ContextCompat.getDrawable(context, R.drawable.pause_icon); // Replace with your play icon
        playPauseButton.setBackground(playDrawable);

        playPauseButton.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                Drawable pauseDrawable = ContextCompat.getDrawable(context, R.drawable.play_icon); // Replace with your pause icon
                playPauseButton.setBackground(pauseDrawable);
            } else {
                videoView.start();
                playPauseButton.setBackground(playDrawable);
            }
        });

// Add Play/Pause toggle button to the RelativeLayout
        pipRelative.addView(playPauseButton);


//        pipRelative.addView(containerView);
        containerView.addView(pipRelative);

//        return pipRelative;
    }

    public void playVideo(String videoUri) {
        videoView.setVideoURI(Uri.parse(videoUri));
        videoView.setOnPreparedListener(mp -> {
            pipRelative.setVisibility(View.VISIBLE); // Show the PiP layout when ready
            videoView.start();
        });
    }

    private void toggleFullscreen() {
        if (isExpanded) {
            // Collapse to default PiP size
            RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                    dpToPx(240),
                    dpToPx(200)
            );
            videoView.setLayoutParams(videoParams);

            RelativeLayout.LayoutParams pipParams = new RelativeLayout.LayoutParams(
                    dpToPx(240),
                    dpToPx(300)
            );
            pipRelative.setLayoutParams(pipParams);

            isExpanded = false; // Set to collapsed state
        } else {
            // Expand to fullscreen size
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;

            RelativeLayout.LayoutParams videoParams = new RelativeLayout.LayoutParams(
                    screenWidth, screenHeight
            );
            videoView.setLayoutParams(videoParams);

            RelativeLayout.LayoutParams pipParams = new RelativeLayout.LayoutParams(
                    screenWidth, screenHeight
            );


            RelativeLayout.LayoutParams fullscreenParams = new RelativeLayout.LayoutParams(
                    dpToPx(40),
                    dpToPx(40)
            );
            fullscreenParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            fullscreenParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            fullscreenButton.setLayoutParams(fullscreenParams);
            pipRelative.setLayoutParams(pipParams);

            isExpanded = true; // Set to expanded state
        }
    }

    private void toggleMute() {
        if (videoView != null && videoView.isPlaying()) {
            MediaPlayer mediaPlayer = getMediaPlayerFromVideoView();
            if (mediaPlayer != null) {
                isMuted = !isMuted;
                mediaPlayer.setVolume(isMuted ? 0 : 1, isMuted ? 0 : 1);

                Drawable icon = ContextCompat.getDrawable(
                        context,
                        isMuted ? R.drawable.mute : R.drawable.unmute // Replace with your icons
                );
                muteUnmuteButton.setBackground(icon);
            }
        }
    }

    private MediaPlayer getMediaPlayerFromVideoView() {
        try {
            // Use reflection to access the MediaPlayer instance inside VideoView
            java.lang.reflect.Field mediaPlayerField = VideoView.class.getDeclaredField("mMediaPlayer");
            mediaPlayerField.setAccessible(true);
            return (MediaPlayer) mediaPlayerField.get(videoView);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to convert dp to px
    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
