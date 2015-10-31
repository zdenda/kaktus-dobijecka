package eu.zkkn.android.kaktus;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Collection of useful methods
 */
public class Helper {

    /**
     * Formats the date as a string with date and time. It respect the localization of device.
     *
     * @param context the application context
     * @param date    the date to format
     * @return the formatted string
     */
    @NonNull
    public static String formatDate(Context context, Date date) {
        return DateFormat.getLongDateFormat(context).format(date)
                + " " + DateFormat.getTimeFormat(context).format(date);
    }

    /**
     * Returns app identification for User-Agent HTTP header
     * @return string for User-Agent header
     */
    public static String getUserAgent() {
        // don't add build type if it is release
        String buildType = "release".equals(BuildConfig.BUILD_TYPE) ? "" : " " + BuildConfig.BUILD_TYPE;
        return String.format("App/%s-%d%s (%s %s; Android/%s)", BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE, buildType, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE);
    }
}
