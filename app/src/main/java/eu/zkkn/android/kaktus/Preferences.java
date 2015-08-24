package eu.zkkn.android.kaktus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Date;

import eu.zkkn.android.kaktus.LastNotification.Notification;

/**
 * App settings
 */
public class Preferences {

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

}
