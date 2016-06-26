package eu.zkkn.android.kaktus.fcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
        Preferences.setFcmAppVersion(context, getAppVersion(context));
    }

    /**
     * @return FCM Token if it has been sent to backend and is for current app version,
     * otherwise return null
     */
    @Nullable
    public static String loadFcmToken(Context context) {
        String token = Preferences.getFcmToken(context);
        if (Preferences.getFcmAppVersion(context) == getAppVersion(context)
                && Preferences.isFcmSentTokenToServer(context)
                && !TextUtils.isEmpty(token)) {
            return token;
        }
        return null;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
