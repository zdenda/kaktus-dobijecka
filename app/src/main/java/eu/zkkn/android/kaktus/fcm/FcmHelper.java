package eu.zkkn.android.kaktus.fcm;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import eu.zkkn.android.kaktus.Preferences;


/**
 *
 */
public class FcmHelper {

    //TODO: Move to common module with backend
    public static final String FCM_TOPIC_NOTIFICATIONS = "notifications";


    public static void saveFcmToken(Context context, String token) {
        Preferences.setFcmToken(context, token);
        Preferences.setFcmSentTokenToServer(context, true);
        Preferences.setSubscribedToNotifications(context, true);
    }

    /**
     * @return FCM Token if it has been sent to backend, is for current app version and the device
     * is subscribed to notifications topic.
     * Otherwise return null
     */
    @Nullable
    public static String loadFcmToken(Context context) {
        String token = Preferences.getFcmToken(context);
        if (!TextUtils.isEmpty(token) && Preferences.isFcmSentTokenToServer(context)
                && Preferences.isSubscribedToNotifications(context)) {
            return token;
        }
        return null;
    }

    public static boolean missingSubscriptionToNotifications(Context context) {
        return Preferences.isFcmSentTokenToServer(context)
                && !Preferences.isSubscribedToNotifications(context);
    }

}
