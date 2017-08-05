package eu.zkkn.android.kaktus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.zkkn.android.kaktus.model.FbApiAttachment;
import eu.zkkn.android.kaktus.model.FbApiAttachments;
import eu.zkkn.android.kaktus.model.FbApiImage;
import eu.zkkn.android.kaktus.model.FbApiPost;
import eu.zkkn.android.kaktus.model.FbApiStoryAttachmentMedia;

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

    @Nullable
    public static String imageUrlFromFbPostAttachment(FbApiPost fbApiPost) {
        // test for every possible null and empty field, since FB API documentation doesn't say
        // which field is mandatory and is always present in the response
        //TODO: can we remove some null checks
        // https://developers.facebook.com/docs/graph-api/reference/v2.9/post/
        FbApiAttachments attachments = fbApiPost.attachments;
        if (attachments != null) {
            FbApiAttachment[] attachmentsData = attachments.attachments;
            if (attachmentsData != null && attachmentsData.length > 0) {
                FbApiAttachment attachment = attachmentsData[0];
                FbApiStoryAttachmentMedia attachmentMedia = attachment.media;
                if (attachmentMedia != null) {
                    FbApiImage image = attachmentMedia.image;
                    if (image != null) {
                        return image.src;
                    }
                }
            }
        }

        return null;
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

    public static void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.generic_alert_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        builder.show();
    }


    public static void viewUri(Context context, @NonNull String uri) {
        context = context.getApplicationContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Sets the background color of border for the {@link ImageView} to match the displayed bitmap
     */
    public static class BackgroundColorCallback implements Callback {

        private ImageView mImageView;
        private View mFrame;

        public BackgroundColorCallback(ImageView imageView, View frame) {
            mImageView = imageView;
            mFrame = frame;
        }

        @Override
        public void onSuccess() {
            mFrame.setVisibility(View.VISIBLE);

            Drawable drawable = mImageView.getDrawable();
            if (!(drawable instanceof BitmapDrawable)) return;

            //If the color in all corners is the same, use it as background for border view
            Bitmap source = ((BitmapDrawable) drawable).getBitmap();
            int maxX = source.getWidth() - 1;
            int maxY = source.getHeight() - 1;
            int corner1 = source.getPixel(0, 0);
            int corner2 = source.getPixel(0, maxY);
            int corner3 = source.getPixel(maxX, 0);
            int corner4 = source.getPixel(maxX, maxY);

            if (corner1 == corner2 && corner2 == corner3 && corner3 == corner4) {
                mFrame.setBackgroundColor(corner1);
            }
            //TODO: maybe hide the border when the corners have a different colors
        }

        @Override
        public void onError() {
            mFrame.setVisibility(View.GONE);
        }

    }

}
