package eu.zkkn.android.kaktus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import eu.zkkn.android.kaktus.LastNotification.Notification;


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
     * FCM token
     */
    private static final String PREF_KEY_FCM_TOKEN = "fcmToken";

    /**
     * Boolean preference that indicates whether FCM token has been sent to the backend server
     */
    private static final String PREF_KEY_FCM_SENT_TOKEN_TO_SERVER = "fcmSentTokenToServer";

    /**
     * Boolean preference that indicates whether the device is subscribed to topic "notifications"
     */
    private static final String PREF_KEY_FCM_TOPIC_NOTIFICATIONS = "fcmTopicNotifications";

    /**
     * Time when was the last notification sent
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_SENT_TIME = "lastNotificationDate";

    /**
     * Time when was the last notification received
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_RECEIVED_TIME =
            "lastNotificationReceivedTime";

    /**
     * Text of the last received notification
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_TEXT = "lastNotificationText";

    /**
     * URI for the last received notification
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_URI = "lastNotificationUri";

    /**
     * Sender of the last received notification
     */
    private static final String PREF_KEY_LAST_NOTIFICATION_FROM = "lastNotificationFrom";

    /**
     * Preference that indicates whether synchronization is enabled
     */
    private static final String PREF_KEY_SYNCHRONIZATION_STATUS = "synchronizationEnabled";

    // Keep old keys for last FB post
    @SuppressWarnings("unused")
    private static final String PREF_KEY_LAST_FB_POST_DATE = "lastKaktusFbPostDate";
    @SuppressWarnings("unused")
    private static final String PREF_KEY_LAST_FB_POST_TEXT = "lastKaktusFbPostText";
    @SuppressWarnings("unused")
    private static final String PREF_KEY_LAST_FB_POST_IMAGE_URL = "lastKaktusFbPostImageUrl";

    /**
     * First call to determine firs run
     */
    private static final String PREF_KEY_FIRST = "firstCall-1";

    /**
     * Don't show the donation message anymore
     * It is not used currently
     */
    private static final String PREF_KEY_HIDE_DONATION = "hideDonation";


    private static SharedPreferences sPreferences;

    private static SharedPreferences getPref(Context context) {
        if (sPreferences == null) {
            sPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context.getApplicationContext());
        }
        return sPreferences;
    }

    public static void setFcmToken(Context context, String token) {
        getPref(context).edit().putString(PREF_KEY_FCM_TOKEN, token).apply();
    }

    public static String getFcmToken(Context context) {
        return getPref(context).getString(PREF_KEY_FCM_TOKEN, "");
    }

    public static void setFcmSentTokenToServer(Context context, boolean isSent) {
        getPref(context).edit().putBoolean(PREF_KEY_FCM_SENT_TOKEN_TO_SERVER, isSent).apply();
    }

    public static boolean isFcmSentTokenToServer(Context context) {
        return getPref(context).getBoolean(PREF_KEY_FCM_SENT_TOKEN_TO_SERVER, false);
    }

    public static void setSubscribedToNotifications(Context context, boolean isSubscribed) {
        getPref(context).edit().putBoolean(PREF_KEY_FCM_TOPIC_NOTIFICATIONS, isSubscribed).apply();
    }

    public static boolean isSubscribedToNotifications(Context context) {
        return getPref(context).getBoolean(PREF_KEY_FCM_TOPIC_NOTIFICATIONS, false);
    }

    public static void setLastNotification(Context context, Notification notification) {
        getPref(context).edit()
                .putLong(PREF_KEY_LAST_NOTIFICATION_SENT_TIME, notification.sent.getTime())
                .putLong(PREF_KEY_LAST_NOTIFICATION_RECEIVED_TIME, notification.received.getTime())
                .putString(PREF_KEY_LAST_NOTIFICATION_TEXT, notification.text)
                .putString(PREF_KEY_LAST_NOTIFICATION_URI, notification.uri)
                .putString(PREF_KEY_LAST_NOTIFICATION_FROM, notification.from)
                .apply();
    }

    public static Notification getLastNotification(Context context) {
        SharedPreferences preferences = getPref(context);
        long sentTimeMs = preferences.getLong(PREF_KEY_LAST_NOTIFICATION_SENT_TIME, 0);
        long receivedTimeMs = preferences.getLong(PREF_KEY_LAST_NOTIFICATION_RECEIVED_TIME, 0);
        String text = preferences.getString(PREF_KEY_LAST_NOTIFICATION_TEXT, null);
        String uri = preferences.getString(PREF_KEY_LAST_NOTIFICATION_URI, null);
        String from = preferences.getString(PREF_KEY_LAST_NOTIFICATION_FROM, null);

        // if there's no last notification
        if (sentTimeMs == 0 || TextUtils.isEmpty(text)) return null;

        return new Notification(new Date(sentTimeMs), new Date(receivedTimeMs), text, uri, from);
    }

    public static void setSyncStatus(Context context, @SyncStatus int status) {
        getPref(context).edit().putInt(PREF_KEY_SYNCHRONIZATION_STATUS, status).apply();
    }

    @SyncStatus
    public static int getSyncStatus(Context context) {
        // annotation check would return error if we didn't check returned value
        @SyncStatus int status = getPref(context).getInt(PREF_KEY_SYNCHRONIZATION_STATUS, SYNC_NOT_SET);
        return (status == SYNC_DISABLED || status == SYNC_ENABLED) ? status : SYNC_NOT_SET;
    }

    /**
     * Check if this is the first time this function is called
     * @param context Context
     * @return true if this is the first time when this function was called, false otherwise
     */
    public static boolean isFirst(Context context) {
        SharedPreferences preferences = getPref(context);
        boolean first = preferences.getBoolean(PREF_KEY_FIRST, true);
        preferences.edit().putBoolean(PREF_KEY_FIRST, false).apply();
        return first;
    }

    public static boolean isDonationHidden(Context context) {
        return getPref(context).getBoolean(PREF_KEY_HIDE_DONATION, false);
    }

    public static void setDonationHidden(Context context, boolean hide) {
        getPref(context).edit().putBoolean(PREF_KEY_HIDE_DONATION, hide).apply();
    }

}
