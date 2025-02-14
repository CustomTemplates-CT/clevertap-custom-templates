package com.clevertap.ct_templates;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.clevertap.ct_templates.nd.CustomButton;
import com.clevertap.ct_templates.nd.NativeDisplayListener;
import com.clevertap.ct_templates.nd.VideoFragment;
import com.clevertap.ct_templates.nd.pip.PipManager;
import com.clevertap.ct_templates.nd.story.StoryAdapter;
import com.clevertap.ct_templates.pn.PushNotificationListener;
import com.clevertap.ct_templates.pn.PushTemplateRenderer;
import org.json.JSONException;
import org.json.JSONObject;

public class TemplateRenderer {

    private static TemplateRenderer instance;

    public static TemplateRenderer getInstance() {
        if (instance == null) {
            return new TemplateRenderer();
        } else {
            return instance;
        }
    }

    public void showNativeDisplay(@IdRes int buttonPushProfile, FragmentManager supportFragmentManager,
                                  JSONObject jsonObject, NativeDisplayListener listener) {
        FragmentTransaction transaction = supportFragmentManager.beginTransaction();
        transaction.add(buttonPushProfile, VideoFragment.newInstance(jsonObject, listener));
        transaction.commit();
    }

    public void renderPiP(Context context, JSONObject jsonObject, ConstraintLayout main) throws JSONException {
        AppCompatActivity activity = new AppCompatActivity();
        PipManager pipManager = new PipManager(context);
        pipManager.createPiPLayout(main);
        String pipUrl = jsonObject.getJSONObject("custom_kv").getString("nd_video_url");
        pipManager.playVideo(pipUrl);
    }

    public void animateButton(Context applicationContext, ViewGroup viewGroup, JSONObject jsonObject,
                              NativeDisplayListener listener) {
        new CustomButton(applicationContext, jsonObject, viewGroup, listener);
    }

    public void showPushNotification(Context applicationContext, Bundle extras, PushNotificationListener listener) {
        PushTemplateRenderer.getInstance().render(applicationContext, extras, listener);
    }

    public StoryAdapter displayStories(Activity context, JSONObject jsonObject,
                                       Boolean shouldAppend) {
        try {
            return new StoryAdapter(context, jsonObject.getJSONArray("content"), (NativeDisplayListener) context, shouldAppend);
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }
    }
}
