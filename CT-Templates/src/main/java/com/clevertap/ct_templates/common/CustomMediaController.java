package com.clevertap.ct_templates.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.clevertap.ct_templates.R;

public class CustomMediaController extends MediaController {

    private ImageButton playPauseButton;
    private SeekBar seekBar;
    private TextView timeLabel;

    public CustomMediaController(Context context) {
        super(context);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        // Inflate custom layout
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.media_controller, null);

        playPauseButton = customView.findViewById(R.id.play_pause_btn);
        seekBar = customView.findViewById(R.id.seekBar);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle play/pause toggle
                if (isShowing()) {
                    hide();
                } else {
                    show();
                }
            }
        });

        // Add the custom layout to MediaController
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        addView(customView, params);
    }

    // You can override other methods to handle seekbar, playback controls, etc.
}

