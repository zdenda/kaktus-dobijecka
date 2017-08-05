package eu.zkkn.android.kaktus;

import android.os.Bundle;
import android.support.annotation.IntDef;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class FirebaseAnalyticsHelper {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EVENT_SYNC_OFF, EVENT_SYNC_ON, EVENT_FB_REFRESH, EVENT_DOBIJECKA_WEB, EVENT_KAKTUS_FB})
    public @interface Event {}
    public static final int EVENT_SYNC_OFF = 1;
    public static final int EVENT_SYNC_ON = 2;
    public static final int EVENT_FB_REFRESH = 3;
    public static final int EVENT_DOBIJECKA_WEB = 4;
    public static final int EVENT_KAKTUS_FB = 5;

    private FirebaseAnalytics mFirebaseAnalytics;


    public FirebaseAnalyticsHelper(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    public void logEvent(@Event int event) {
        String name;
        Bundle params = new Bundle();

        switch (event) {
            case EVENT_SYNC_OFF:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "settings_fb_sync");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CheckBox");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "disable");
                break;
            case EVENT_SYNC_ON:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "settings_fb_sync");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CheckBox");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "enable");
                break;
            case EVENT_FB_REFRESH:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "main_fb_refresh");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "refresh");
                break;
            case EVENT_DOBIJECKA_WEB:
                name = FirebaseAnalytics.Event.VIEW_ITEM;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "main_notification");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CardView");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "dobijecka_web");
                break;
            case EVENT_KAKTUS_FB:
                name = FirebaseAnalytics.Event.VIEW_ITEM;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "main_fb_post");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CardView");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "kaktus_fb");
                break;
            default:
                throw new RuntimeException("Unsupported Firebase Analytics Event ID: " + event);
        }

        mFirebaseAnalytics.logEvent(name, params);
    }

}
