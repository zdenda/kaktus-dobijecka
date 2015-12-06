package eu.zkkn.android.kaktus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import eu.zkkn.android.kaktus.LastFbPost.FbPost;
import eu.zkkn.android.kaktus.LastNotification.Notification;

//TODO: Marshmallow has auto back up, so check which preferences shouldn't be backed up

/**
 * App settings
 */
public class Preferences {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SYNC_NOT_SET, SYNC_DISABLED, SYNC_ENABLED})
    public @interface SyncStatus {}
    public static final int SYNC_NOT_SET = 0;
    public static final int SYNC_DISABLED = 1;
    public static final int SYNC_ENABLED = 2;

    /**
     * GCM token
     */
    private static final String PREF_KEY_GCM_TOKEN = "gcmToken";

    /**
     * Boolean preference that indicates whether GCM token has been sent to the backend server
     */
    private static final String PREF_KEY_GCM_SENT_TOKEN_TO_SERVER = "gcmSentTokenToServer";

    /**
     * App version when GCM token was retrieved
     */
    private static final String PREF_KEY_GCM_APP_VERSION = "gcmAppVersion";

    /**
     * Time when was the last notification received
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_DATE = "lastNotificationDate";

    /**
     * Text of the last received notification
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_TEXT = "lastNotificationText";

    /**
     * Preference that indicates whether synchronization is enabled
     */
    private static final String PREF_KEY_SYNCHRONIZATION_STATUS = "synchronizationEnabled";

    /**
     * Time when was the last post on Kaktus' Facebook page created
     */
    private static final String PREF_KEY_LAST_FB_POST_DATE = "lastKaktusFbPostDate";

    /**
     * Text of the last post on Kaktus' Facebook page
     */
    private static final String PREF_KEY_LAST_FB_POST_TEXT = "lastKaktusFbPostText";


    private static SharedPreferences sPreferences;

    private static SharedPreferences getPref(Context context) {
        if (sPreferences == null) {
            sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sPreferences;
    }

    public static void setGcmToken(Context context, String token) {
        getPref(context).edit().putString(PREF_KEY_GCM_TOKEN, token).commit();
    }

    public static String getGcmToken(Context context) {
        return getPref(context).getString(PREF_KEY_GCM_TOKEN, "");
    }

    public static void setGcmSentTokenToServer(Context context, boolean isSent) {
        getPref(context).edit().putBoolean(PREF_KEY_GCM_SENT_TOKEN_TO_SERVER, isSent).commit();
    }

    public static boolean isGcmSentTokenToServer(Context context) {
        return getPref(context).getBoolean(PREF_KEY_GCM_SENT_TOKEN_TO_SERVER, false);
    }

    public static void setGcmAppVersion(Context context, int version) {
        getPref(context).edit().putInt(PREF_KEY_GCM_APP_VERSION, version).commit();
    }

    public static int getGcmAppVersion(Context context) {
        return getPref(context).getInt(PREF_KEY_GCM_APP_VERSION, 0);
    }

    public static void setLastNotification(Context context, Notification notification) {
        getPref(context).edit().putLong(PREF_KEY_LAST_NOTIFICATION_DATE, notification.date.getTime())
                .putString(PREF_KEY_LAST_NOTIFICATION_TEXT, notification.text).commit();
    }

    public static Notification getLastNotification(Context context) {
        SharedPreferences preferences = getPref(context);
        Long unixTimeMs = preferences.getLong(PREF_KEY_LAST_NOTIFICATION_DATE, 0);
        String text = preferences.getString(PREF_KEY_LAST_NOTIFICATION_TEXT, null);

        // if there's no last notification
        if (unixTimeMs == 0 || TextUtils.isEmpty(text)) return null;

        return new Notification(new Date(unixTimeMs), text);
    }

    public static void setLastFbPost(Context context, FbPost fbPost) {
        getPref(context).edit().putLong(PREF_KEY_LAST_FB_POST_DATE, fbPost.date.getTime())
                .putString(PREF_KEY_LAST_FB_POST_TEXT, fbPost.text).commit();
    }

    public static FbPost getLastFbPost(Context context) {
        SharedPreferences preferences = getPref(context);
        Long unixTimeMs = preferences.getLong(PREF_KEY_LAST_FB_POST_DATE, 0);
        String text = preferences.getString(PREF_KEY_LAST_FB_POST_TEXT, null);

        // if there's no last notification
        if (unixTimeMs == 0 || TextUtils.isEmpty(text)) return null;

        return new FbPost(new Date(unixTimeMs), text);
    }

    public static void setSyncStatus(Context context, @SyncStatus int status) {
        getPref(context).edit().putInt(PREF_KEY_SYNCHRONIZATION_STATUS, status).commit();
    }

    @SyncStatus
    public static int getSyncStatus(Context context) {
        // annotation check would return error if we didn't check returned value
        @SyncStatus int status = getPref(context).getInt(PREF_KEY_SYNCHRONIZATION_STATUS, SYNC_NOT_SET);
        return (status == SYNC_DISABLED || status == SYNC_ENABLED) ? status : SYNC_NOT_SET;
    }

}
