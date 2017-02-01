package eu.zkkn.android.kaktus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Collection of useful methods
 */
public class Helper {

    public static final long MIN_IN_S = 60;
    public static final long HOUR_IN_S = MIN_IN_S * 60;
    public static final long DAY_IN_S = HOUR_IN_S * 24;

    public static final String FB_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

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
     * Parses date strings from Facebook API
     * @param string String in format {@link #FB_DATE_PATTERN}
     * @return Date object
     */
    @Nullable
    public static Date parseFbDate(String string) {
        SimpleDateFormat fbDateFormat = new SimpleDateFormat(FB_DATE_PATTERN, Locale.US);
        try {
            return fbDateFormat.parse(string);
        } catch (ParseException e) {
            return new Date();
        }
    }

    /**
     * Returns app identification for User-Agent HTTP header
     * @return string for User-Agent header
     */
    public static String getUserAgent() {
        // don't add build type if it is release
        String buildType = "release".equals(BuildConfig.BUILD_TYPE) ? "" : " " + BuildConfig.BUILD_TYPE;
        return String.format(Locale.US, "App/%s-%d%s (%s %s; Android/%s)", BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE, buildType, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE);
    }

    public static Spanned formatHtml(String formatWithHtml, Object... args) {
        String htmlText = String.format(formatWithHtml, args);
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            spanned = Html.fromHtml(htmlText);
        }
        return spanned;
    }

    public static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

}
