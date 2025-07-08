package com.clevertap.ct_templates.pn;


import android.Manifest;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.clevertap.ct_templates.R;
import com.clevertap.ct_templates.common.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import pl.droidsonroids.gif.GifDrawable;

public class PushTemplateRenderer {

    private static PushTemplateRenderer instance;

    public static PushTemplateRenderer getInstance() {
        if (instance == null) {
            return new PushTemplateRenderer();
        } else {
            return instance;
        }
    }

    public void render(Context applicationContext, Bundle extras, PushNotificationListener listener) {

        switch (Objects.requireNonNull(extras.getString("pt_id"))) {
            case "pt_progress_bar":
                renderProgressBarNotification(applicationContext, extras, listener);
                break;
            case "pt_gif":
                renderGIFNotification(applicationContext, extras, listener);
                break;
            case "pt_coupon":
                renderCouponNotification(applicationContext, extras, listener);
                break;
            default:
                listener.onPushFailed();
                break;
        }
    }

    private void renderGIFNotification(Context context, Bundle extras, PushNotificationListener listener) {
        try {
            int notificationId = new Random().nextInt(60000);

            String gifUrl = extras.getString("pt_gif");
            String title = extras.getString("pt_title");
            String message = extras.getString("pt_msg");
            String deepLink = extras.getString("pt_dl");
            String channelId = extras.getString("wzrk_cid");

            if (gifUrl == null || title == null || message == null || deepLink == null || channelId == null) {
                throw new IllegalArgumentException("Missing required extras");
            }

            List<Bitmap> frames = extractOptimizedFrames(context, gifUrl, 15);
            if (frames.isEmpty()) {
                Log.e("GIF_NOTIFICATION", "No frames extracted.");
                listener.onPushFailed();
                return;
            }

            int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int textColor = (nightMode == Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE : Color.BLACK;

            RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.gif_collapsed);
            RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.gif_notification);

            expandedView.setTextViewText(R.id.title, title);
            expandedView.setTextColor(R.id.title, textColor);
            expandedView.setTextViewText(R.id.msg, message);
            expandedView.setTextColor(R.id.msg, textColor);

            for (Bitmap bitmap : frames) {
                RemoteViews frameView = new RemoteViews(context.getPackageName(), R.layout.image_view);
                frameView.setImageViewBitmap(R.id.fimg, bitmap);
                collapsedView.addView(R.id.view_flipper, frameView);
                expandedView.addView(R.id.view_flipper, frameView);
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.custom_progress_drawable)
                    .setCustomContentView(collapsedView)
                    .setCustomBigContentView(expandedView)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
            listener.onPushRendered();

        } catch (Exception e) {
            Log.e("GIF_NOTIFICATION", "Notification rendering failed", e);
            listener.onPushFailed();
        }
    }
    
    public static List<Bitmap> extractOptimizedFrames(Context context, String gifUrl, int maxFrames) {
        List<Bitmap> frames = new ArrayList<>();
        try (InputStream inputStream = new BufferedInputStream(new URL(gifUrl).openStream())) {
            GifDrawable gifDrawable = new GifDrawable(inputStream);
            int total = gifDrawable.getNumberOfFrames();
            Log.d("GIF_FRAMES", "Total frames in gif: " + total);

            Set<Integer> indices = new LinkedHashSet<>();

            if (total <= maxFrames) {
                for (int i = 1; i < total; i += 2) indices.add(i);
                if ((total - 1) % 2 == 0) indices.add(total - 1);
            } else {
                indices.add(0);
                indices.add(total - 1);
                int step = (total - 2) / (maxFrames - 2);
                for (int i = 1; i < maxFrames - 1; i++) {
                    indices.add(i * step);
                }
            }

            for (int index : indices) {
                Bitmap frame = gifDrawable.seekToFrameAndGet(index);
                Bitmap scaled = Bitmap.createScaledBitmap(frame, 400, 200, true);
                frames.add(scaled);
                Log.d("GIF_FRAMES", "Selected frame index: " + index);
            }

            Log.d("GIF_FRAMES", "Total selected frames: " + frames.size());

        } catch (Exception e) {
            Log.e("GIF_FRAMES", "Frame extraction failed: " + e.getMessage(), e);
        }

        return frames;
    }

    private void renderProgressBarNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {

        try {
            NotificationCompat.Builder builder;
            NotificationManagerCompat notificationManager;
            int notificationId = new Random().nextInt(60000);

            notificationManager = NotificationManagerCompat.from(applicationContext);
            builder = new NotificationCompat.Builder(applicationContext, extras.getString("wzrk_cid"));

            String pushTitleStart = extras.getString("pt_title");
            String timerThreshold = extras.getString("pt_timer_threshold");
            String pushTitleEnd = extras.getString("pt_title_alt");
            String pushMessageEnd = extras.getString("pt_msg_alt");
            String image = extras.getString("pt_big_img");
            String deepLink = extras.getString("pt_dl");

            if (pushTitleStart == null || timerThreshold == null || pushTitleEnd == null || pushMessageEnd == null || image == null || deepLink == null) {
                throw new IllegalArgumentException();
            }

            int PROGRESS_STEPS = 5;

            RemoteViews collapsed = new RemoteViews(applicationContext.getPackageName(), R.layout.custom_layout_collapsed);
            collapsed.setTextViewText(R.id.title, pushTitleStart);
            collapsed.setOnClickPendingIntent(R.id.wrapper, Utils.getActivityIntent(extras, applicationContext));

            RemoteViews expanded = new RemoteViews(applicationContext.getPackageName(), R.layout.custom_layout_expanded);
            Utils.loadImageURLIntoRemoteView(R.id.big_image, image, expanded, applicationContext);
            expanded.setTextViewText(R.id.title, pushTitleStart);
            expanded.setOnClickPendingIntent(R.id.wrapper, Utils.getActivityIntent(extras, applicationContext));

            for (int i = 0; i < PROGRESS_STEPS; i++) {
                int drawableId = applicationContext.getResources().getIdentifier("progress_" + (i + 1), "drawable", applicationContext.getPackageName());
                RemoteViews imageContentView = new RemoteViews(applicationContext.getPackageName(), R.layout.image_progress);
                imageContentView.setImageViewResource(R.id.fimg, drawableId);

                collapsed.addView(R.id.progress_flipper, imageContentView);
                expanded.addView(R.id.progress_flipper, imageContentView);
            }
            collapsed.setInt(R.id.view_flipper, "setFlipInterval", 500);
            expanded.setInt(R.id.view_flipper, "setFlipInterval", 500);

            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(notificationId, getNotification(pushTitleStart, builder, collapsed, expanded).build());

            listener.onPushRendered();

        } catch (Exception e) {
            listener.onPushFailed();
        }
    }

    private NotificationCompat.Builder getNotification(String title, NotificationCompat.Builder builder, RemoteViews collapsed, RemoteViews expanded) {
        builder.setContentTitle(title).setSmallIcon(R.drawable.custom_progress_drawable).setCustomContentView(collapsed).setCustomBigContentView(expanded).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder;
    }

    private void renderCouponNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        try {
            String pushTitle = extras.getString("pt_title");
            String pushMessage = extras.getString("pt_msg");
            String deepLink = extras.getString("pt_dl");
            String discount = extras.getString("pt_discount");
            String disc_title = extras.getString("pt_discount_txt");
            String couponCode = extras.getString("pt_cc");

            if (pushTitle == null || pushMessage == null || couponCode == null) {
                throw new IllegalArgumentException();
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, Objects.requireNonNull(extras.getString("wzrk_cid")));
            int notificationId = new Random().nextInt(60000);


            RemoteViews couponCodeView = new RemoteViews(applicationContext.getPackageName(), R.layout.coupon_code_expanded);
            couponCodeView.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeView.setTextViewText(R.id.notification_body, pushMessage);
            couponCodeView.setTextViewText(R.id.notification_discount, discount);
            couponCodeView.setTextViewText(R.id.notification_coupon, couponCode);
            couponCodeView.setTextViewText(R.id.notification_coupon_text, disc_title);

            RemoteViews couponCodeViewsCollapsed = new RemoteViews(applicationContext.getPackageName(), R.layout.coupon_code_collpsed);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_title, pushTitle);
            couponCodeViewsCollapsed.setTextViewText(R.id.notification_coupon, couponCode);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.putExtra("coupon", couponCode);
            intent.putExtra("nid", notificationId);
            intent.setAction("Dismiss");

            if (intent.resolveActivity(applicationContext.getPackageManager()) != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                couponCodeView.setOnClickPendingIntent(R.id.clickarea, pendingIntent);
                couponCodeViewsCollapsed.setOnClickPendingIntent(R.id.notification_coupon, pendingIntent);

                builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setSmallIcon(R.drawable.pt_dot_sep).setCustomContentView(couponCodeViewsCollapsed).setCustomBigContentView(couponCodeView)// Set custom notification layout
                        .setAutoCancel(true);

                if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                notificationManager.notify(notificationId, builder.build());
            } else {
                Toast.makeText(applicationContext, "Invalid Page!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.d("Here is the exception", e.getLocalizedMessage());
        }

    }
}

class GifUtils {

    public static List<Bitmap> extractGifFrames(Context context, String gifUrl) {
        List<Bitmap> frameList = new ArrayList<>();
        try {
            // Buffered stream improves compatibility with streamed content
            InputStream inputStream = new BufferedInputStream(new URL(gifUrl).openStream());
            GifDrawable gifDrawable = new GifDrawable(inputStream);

            int frameCount = gifDrawable.getNumberOfFrames();
            Log.d("GIF_FRAMES", "Extracted frame count: " + frameCount);

            for (int i = 0; i < frameCount; i++) {
                Bitmap bmp = gifDrawable.seekToFrameAndGet(i);
                frameList.add(bmp);
            }

        } catch (Exception e) {
            Log.e("GIF_FRAMES", "Error decoding gif: " + e.getMessage(), e);
        }
        return frameList;
    }
}