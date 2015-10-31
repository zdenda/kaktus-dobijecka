package eu.zkkn.android.kaktus.gcm;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import eu.zkkn.android.kaktus.Preferences;

/**
 *
 */
public class GcmHelper {

    public static void saveGcmToken(Context context, String token) {
        Preferences.setGcmToken(context, token);
        Preferences.setGcmSentTokenToServer(context, true);
        Preferences.setGcmAppVersion(context, getAppVersion(context));
    }

    /**
     * @return GCM Token if it has been sent to backend and is for current app version,
     * otherwise return null
     */
    @Nullable
    public static String loadGcmToken(Context context) {
        String token = Preferences.getGcmToken(context);
        if (Preferences.getGcmAppVersion(context) == getAppVersion(context)
                && Preferences.isGcmSentTokenToServer(context)
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
