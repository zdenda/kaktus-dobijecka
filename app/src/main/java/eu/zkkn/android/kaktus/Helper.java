package eu.zkkn.android.kaktus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.Date;
import java.util.Locale;


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
        //noinspection ConstantConditions
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
            spanned = Html.fromHtml(htmlText);
        }
        return spanned;
    }

    public static void copyToClipboard(Context context, String text) {
        copyToClipboard(context, text, text);
    }

    public static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.generic_alert_button_ok,
                (dialog, which) -> dialog.dismiss()
        );
        builder.show();
    }


    public static void viewUri(Context context, @NonNull String uri) {
        context = context.getApplicationContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    @Nullable
    public static Intent getAppIntent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

}
