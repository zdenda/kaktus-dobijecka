package eu.zkkn.android.kaktus;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;


public class FirebaseAnalyticsHelper {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EVENT_SYNC_OFF, EVENT_SYNC_ON, EVENT_FB_REFRESH, EVENT_DOBIJECKA_WEB, EVENT_KAKTUS_FB,
            EVENT_HIDE_DONATION, EVENT_DONATE, EVENT_DONATE_ABOUT, EVENT_FCM_RECEIVED})
    public @interface Event {}
    public static final int EVENT_SYNC_OFF = 1;
    public static final int EVENT_SYNC_ON = 2;
    public static final int EVENT_FB_REFRESH = 3;
    public static final int EVENT_DOBIJECKA_WEB = 4;
    public static final int EVENT_KAKTUS_FB = 5;
    public static final int EVENT_HIDE_DONATION = 6;
    public static final int EVENT_DONATE = 7;
    public static final int EVENT_DONATE_ABOUT = 8;
    public static final int EVENT_FCM_RECEIVED = 9;

    private FirebaseAnalytics mFirebaseAnalytics;


    public FirebaseAnalyticsHelper(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    public void logEvent(@Event int event) {
        logEvent(event, new Bundle());
    }

    public void logEvent(@Event int event, String contentType) {
        Bundle params = new Bundle(1);
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        logEvent(event, params);
    }

    private void logEvent(@Event int event, @NonNull Bundle params) {
        String name;
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
            case EVENT_HIDE_DONATION:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "main_donation_hide");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "hide");
                break;
            case EVENT_DONATE:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "main_donation_send");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "make_donation");
                break;
            case EVENT_DONATE_ABOUT:
                name = FirebaseAnalytics.Event.SELECT_CONTENT;
                params.putString(FirebaseAnalytics.Param.ITEM_ID, "about_donation_send");
                params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                params.putString(FirebaseAnalytics.Param.ITEM_NAME, "make_donation");
                break;
            case EVENT_FCM_RECEIVED:
                name = "fcm_message_received";
                break;
            default:
                throw new RuntimeException("Unsupported Firebase Analytics Event ID: " + event);
        }

        mFirebaseAnalytics.logEvent(name, params);
    }

}
