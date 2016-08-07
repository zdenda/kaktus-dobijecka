package eu.zkkn.android.kaktus.fcm;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import eu.zkkn.android.kaktus.Preferences;

/**
 *
 */
public class FcmHelper {

    public static void saveFcmToken(Context context, String token) {
        Preferences.setFcmToken(context, token);
        Preferences.setFcmSentTokenToServer(context, true);
    }

    /**
     * @return FCM Token if it has been sent to backend and is for current app version,
     * otherwise return null
     */
    @Nullable
    public static String loadFcmToken(Context context) {
        String token = Preferences.getFcmToken(context);
        if (!TextUtils.isEmpty(token) && Preferences.isFcmSentTokenToServer(context)) {
            return token;
        }
        return null;
    }

}
