package com.clevertap.ct_templates.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static void loadImageURLIntoRemoteView(int imageViewID, String imageUrl,
                                                  RemoteViews remoteViews, Context context) {
        Bitmap image = getBitmapFromURL(imageUrl, context);
        if (image != null) {
            remoteViews.setImageViewBitmap(imageViewID, image);
        }
    }

    public static void loadImageURLIntoImageView(ImageView imageView, String imageUrl, Activity context) {
        new Thread(() -> {
            Bitmap image = getBitmapFromURL(imageUrl, context);
            if (image != null) {
                context.runOnUiThread(() -> imageView.setImageBitmap(image));
            }
        }).start();
    }

    public static boolean isNetworkOnline(Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                // lets be optimistic, if we are truly offline we handle the exception
                return true;
            }
            @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Throwable ignore) {
            // lets be optimistic, if we are truly offline we handle the exception
            return true;
        }
    }

    private static Bitmap getBitmapFromURL(String srcUrl, @Nullable Context context) {
        if (context != null) {
            boolean isNetworkOnline = isNetworkOnline(context);
            if (!isNetworkOnline) {
                return null;
            }
        }

        // Safe bet, won't have more than three /s
        srcUrl = srcUrl.replace("///", "/");
        srcUrl = srcUrl.replace("//", "/");
        srcUrl = srcUrl.replace("http:/", "http://");
        srcUrl = srcUrl.replace("https:/", "https://");
        HttpURLConnection connection = null;
        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setUseCaches(true);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            // might be -1: server did not report the length
            long fileLength = connection.getContentLength();
            boolean isGZipEncoded = (connection.getContentEncoding() != null &&
                    connection.getContentEncoding().contains("gzip"));

            // download the file
            InputStream input = connection.getInputStream();

            byte[] data = new byte[16384];
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                buffer.write(data, 0, count);
            }

            byte[] tmpByteArray = new byte[16384];
            long totalDownloaded = total;

            if (isGZipEncoded) {
                InputStream is = new ByteArrayInputStream(buffer.toByteArray());
                ByteArrayOutputStream decompressedFile = new ByteArrayOutputStream();
                GZIPInputStream gzipInputStream = new GZIPInputStream(is);
                total = 0;
                int counter;
                while ((counter = gzipInputStream.read(tmpByteArray)) != -1) {
                    total += counter;
                    decompressedFile.write(tmpByteArray, 0, counter);
                }
                if (fileLength != -1 && fileLength != totalDownloaded) {
                    return null;
                }
                return BitmapFactory.decodeByteArray(decompressedFile.toByteArray(), 0, (int) total);
            }

            if (fileLength != -1 && fileLength != totalDownloaded) {
                return null;
            }
            return BitmapFactory.decodeByteArray(buffer.toByteArray(), 0, (int) totalDownloaded);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Throwable t) {

            }
        }
    }


    public static PendingIntent getActivityIntent(@NonNull Bundle extras, @NonNull Context context) {
        Intent launchIntent;
        if (extras.containsKey("pt_dl") && extras.getString("pt_dl") != null) {
            launchIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(extras.getString("pt_dl")));
        } else {
            launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent == null) {
                return null;
            }
        }

        launchIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Take all the properties from the notif and add it to the intent
        launchIntent.putExtras(extras);
        launchIntent.removeExtra("wzrk_acts");

        int flagsLaunchPendingIntent = PendingIntent.FLAG_UPDATE_CURRENT;
        flagsLaunchPendingIntent |= PendingIntent.FLAG_IMMUTABLE;

        return PendingIntent.getActivity(context, (int) System.currentTimeMillis(), launchIntent,
                flagsLaunchPendingIntent);
    }
}

