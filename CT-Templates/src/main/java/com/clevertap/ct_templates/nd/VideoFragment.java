package com.clevertap.ct_templates.nd;


import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.Gravity.TOP;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.clevertap.ct_templates.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class VideoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static VideoFragment videoFragment;
    ImageView cancelBtn;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private VideoView videoView;
    private FrameLayout draggableFrame;
    private final JSONObject cleverTapDisplayUnit;
    private float dX, dY;
    private String videoUrl;
    private Boolean isMovebale = true;
    private String gravity = "end-bottom";
    private int playCount = 0;
    private Integer maxPlays = 1;
    private Boolean isCancel = true;

    public VideoFragment(JSONObject displayUnit) {
        this.cleverTapDisplayUnit = displayUnit;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param displayUnit
     * @param listener
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(JSONObject displayUnit, NativeDisplayListener listener) {
        if (videoFragment == null) {
            return new VideoFragment(displayUnit);
        } else {
            return videoFragment;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        cancelBtn = view.findViewById(R.id.cancel_button);
        videoView = view.findViewById(R.id.videoView);
        draggableFrame = view.findViewById(R.id.draggable_frame);

        try {

            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);


            ViewGroup.LayoutParams params = draggableFrame.getLayoutParams();
            params.height = (int) (displayMetrics.heightPixels / 4);
            params.width = (int) (displayMetrics.widthPixels / 3);
            draggableFrame.setLayoutParams(params);

            videoUrl = cleverTapDisplayUnit.getJSONObject("custom_kv").getString("nd_video_url");

            isMovebale = Boolean.parseBoolean(cleverTapDisplayUnit.getJSONObject("custom_kv").getString("nd_movable"));
            gravity = cleverTapDisplayUnit.getJSONObject("custom_kv").getString("nd_position");
            maxPlays = Integer.parseInt(Objects.requireNonNull(cleverTapDisplayUnit.getJSONObject("custom_kv").getString("nd_loop")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) draggableFrame.getLayoutParams();

        switch (gravity) {
            case "start-top":
                params.gravity = TOP | START;
                draggableFrame.setLayoutParams(params);
                break;
            case "start-bottom":
                params.gravity = BOTTOM | START;
                draggableFrame.setLayoutParams(params);
                break;
            case "end-top":
                params.gravity = TOP | END;
                draggableFrame.setLayoutParams(params);
                break;
            case "end-bottom":
                params.gravity = BOTTOM | END;
                draggableFrame.setLayoutParams(params);
                break;
        }
        if (maxPlays != null) {
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playCount++;
                    if (playCount < maxPlays) {
                        videoView.start();
                    } else {
                        stopVideo();
                    }
                }
            });
        }
        Uri videoUri = Uri.parse(videoUrl);
        System.out.println(videoUri);
        videoView.setVideoURI(videoUri);
        videoView.start();
        if (isMovebale) {
            isCancel = false;
            draggableFrame.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = view.getX() - event.getRawX();
                            dY = view.getY() - event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;
                            newX = Math.max(0, Math.min(newX, getScreenWidth() - view.getWidth()));
                            newY = Math.max(0, Math.min(newY, getScreenHeight() - view.getHeight()));
                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (isOutOfScreenOnRight(view)) {
                                stopVideo();
                            }
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                private boolean isOutOfScreenOnRight(View view) {
                    System.out.println("Inside isOutOfScreenOnRight");
                    // Get the screen width
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    float viewRightEdge = view.getX() + view.getWidth();
                    //Added "+100" for videoView to go out of screen from rightside
                    return viewRightEdge >= screenWidth + 100;
                }

                private int getScreenWidth() {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    //Added "+100" for videoView to go out of screen from rightside
                    return displayMetrics.widthPixels + 100;
                }

                private int getScreenHeight() {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    return displayMetrics.heightPixels;
                }
            });
        }
        if (!isCancel) {
            cancelBtn.setVisibility(View.GONE);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopVideo();
                }
            });
        } else {
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopVideo();
                }
            });
        }
        return view;
    }

    private void stopVideo() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.remove(this);
            transaction.commitAllowingStateLoss();
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        stopVideo();
    }
}
