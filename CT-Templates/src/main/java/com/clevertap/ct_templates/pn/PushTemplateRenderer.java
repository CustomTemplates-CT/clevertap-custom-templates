package com.clevertap.ct_templates.pn;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class PushTemplateRenderer {

    private static PushTemplateRenderer instance;

    public static PushTemplateRenderer getInstance() {
        if (instance == null) {
            return new PushTemplateRenderer();
        } else {
            return instance;
        }
    }

    static ArrayList<String> getImageListFromExtras(Bundle extras, String size) {
        ArrayList<String> imageList = new ArrayList<>();
        for (String key : extras.keySet()) {
            if (key.contains("pt_" + size + "_img")) {
                imageList.add(extras.getString(key));
            }
        }
        return imageList;
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

    private void renderGIFNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        try {
            int notificationId = new Random().nextInt(60000);

            ArrayList<String> smallImageList = getImageListFromExtras(extras, "small");
            ArrayList<String> largeImageList = getImageListFromExtras(extras, "large");

            String pushTitle = extras.getString("pt_title");
            String pushMessage = extras.getString("pt_msg");
            String deepLink = extras.getString("pt_dl");

            if (pushTitle == null || pushMessage == null || smallImageList.isEmpty() || largeImageList.isEmpty() || deepLink == null) {
                throw new IllegalArgumentException();
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, extras.getString("wzrk_cid"));


            RemoteViews gifExpandedContentView = new RemoteViews(applicationContext.getPackageName(), R.layout.gif_notification);
            RemoteViews gifCollapsedContentView = new RemoteViews(applicationContext.getPackageName(), R.layout.gif_collapsed);

            gifExpandedContentView.setTextViewText(R.id.title, pushTitle);
            gifExpandedContentView.setTextViewText(R.id.msg, pushMessage);

            for (String image : smallImageList) {
                RemoteViews imageContentView = new RemoteViews(applicationContext.getPackageName(), R.layout.image_view);
                Utils.loadImageURLIntoRemoteView(R.id.fimg, image, imageContentView, applicationContext);
                gifCollapsedContentView.addView(R.id.view_flipper, imageContentView);
            }

            for (String image : largeImageList) {
                RemoteViews imageContentView = new RemoteViews(applicationContext.getPackageName(), R.layout.image_view);
                Utils.loadImageURLIntoRemoteView(R.id.fimg, image, imageContentView, applicationContext);
                gifExpandedContentView.addView(R.id.view_flipper, imageContentView);
            }

            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

//            builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                    .setSmallIcon(R.drawable.pt_dot_sep)
//                    .setCustomContentView(gifCollapsedContentView)
//                    .setCustomBigContentView(gifExpandedContentView)
//                    .setOnlyAlertOnce(true)
//                    .setAutoCancel(true);

            notificationManager.notify(notificationId, builder.setContentTitle(pushTitle)
                    .setSmallIcon(R.drawable.custom_progress_drawable)
                    .setCustomContentView(gifCollapsedContentView)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomBigContentView(gifExpandedContentView)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build());

            listener.onPushRendered();
        } catch (Exception e) {
            listener.onPushFailed();
        }
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

