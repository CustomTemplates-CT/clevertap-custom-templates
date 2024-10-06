package com.clevertap.ct_templates.nd;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONObject;

public class CustomButton extends AppCompatButton {

    private Paint paintTR;
    private Paint paintLB;
    private int[] colors = {Color.GREEN, Color.CYAN}; // Two colors to animate between
    private final int currentIndex = 0;
    private long duration = 5000;
    private ValueAnimator animator;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private CustomButton(Context context, int[] colors, long duration) {
        super(context);
        this.colors = colors;
        this.duration = duration;
        init();
    }

    public CustomButton(Context context, JSONObject unit, ViewGroup parentLayout, NativeDisplayListener listener) {
        super(context);
        try {
            String color1 = unit.getJSONObject("custom_kv").getString("nd_animation_color1");
            String color2 = unit.getJSONObject("custom_kv").getString("nd_animation_color2");
            String bgcolor = unit.getJSONObject("custom_kv").getString("nd_button_color");
            String textColor = unit.getJSONObject("custom_kv").getString("nd_button_text_color");
            String btnText = unit.getJSONObject("custom_kv").getString("nd_button_text");
            String btnID = unit.getJSONObject("custom_kv").getString("nd_button_id");
            long duration = Long.parseLong(unit.getJSONObject("custom_kv").getString("nd_duration"));

            int resourceId = getResources().getIdentifier(btnID, "id", context.getPackageName());

            Button nativeButton = parentLayout.findViewById(resourceId);
            int parsedcolor1 = Color.parseColor(color1);
            int parsedcolor2 = Color.parseColor(color2);
            int[] colors = {parsedcolor1, parsedcolor2};
            CustomButton customButton = new CustomButton(context, colors, duration);

            customButton.setLayoutParams(nativeButton.getLayoutParams());
            customButton.setPadding(nativeButton.getPaddingLeft(), nativeButton.getTotalPaddingTop(),
                    nativeButton.getPaddingRight(), nativeButton.getPaddingBottom());
            customButton.setText(btnText);
            customButton.setTextAlignment(nativeButton.getTextAlignment());
            customButton.setId(nativeButton.getId());
            customButton.setBackgroundColor(Color.parseColor(bgcolor));
            customButton.setTextColor(Color.parseColor(textColor));

            customButton.setOnClickListener(view -> nativeButton.performClick());

            parentLayout.removeView(nativeButton);
            parentLayout.addView(customButton);
            init();

            listener.onSuccess("nd_custom_button");

        } catch (Exception e) {
            listener.onFailure("nd_custom_button");
            Log.e("Glow Button Error", String.valueOf(e));
        }
    }

    private void init() {
        paintTR = new Paint();
        paintTR.setStyle(Paint.Style.STROKE);
        paintTR.setStrokeWidth(25);
        paintLB = new Paint();
        paintLB.setStyle(Paint.Style.STROKE);
        paintLB.setStrokeWidth(25);

        startAnimation();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            updateColors(animatedValue);
            invalidate();
        });
        animator.start();
        handler.postDelayed(this::stopAnimation, duration);
    }

    public void stopAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private void updateColors(float animatedValue) {
        int nextIndex = (currentIndex + 1) % colors.length;

        // Interpolate colors based on animatedValue
        paintTR.setColor(lerpColor(colors[currentIndex], colors[nextIndex], animatedValue));
        paintLB.setColor(lerpColor(colors[nextIndex], colors[currentIndex], animatedValue));
    }

    private int lerpColor(int color1, int color2, float ratio) {
        float inverseRatio = 1 - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRatio);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRatio);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRatio);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        canvas.drawLine(0, 0, width, 0, paintTR); // Top
        canvas.drawLine(0, 0, 0, height, paintLB); // Left
        canvas.drawLine(width, 0, width, height, paintTR); // Right
        canvas.drawLine(0, height, width, height, paintLB); // Bottom
    }

}
