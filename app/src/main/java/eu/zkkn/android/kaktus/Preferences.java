package eu.zkkn.android.kaktus;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

}
