package eu.zkkn.android.kaktus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


public class NotificationHelper {

    public static final String DOBIJECKA_CHANNEL_ID = "dobijecka";
    public static final int DOBIJECKA_NOTIFICATION_ID = 1;


    // NotificationManagerCompat uses app context, so it doesn't leak activity nor service
    @SuppressLint("StaticFieldLeak")
    private static NotificationManagerCompat sNotificationManager;


    public static NotificationCompat.Builder getDefaultBuilder(Context context, String channelId) {
        if (!channelExists(context, channelId) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, channelId);
        }
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
    }

    public static boolean areNotificationsEnabled(Context context) {
        return getNotificationManager(context).areNotificationsEnabled();
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, DOBIJECKA_CHANNEL_ID);
        }
    }

    public static void notify(Context ctx, int notificationId, @NonNull Notification notification) {
        getNotificationManager(ctx).notify(notificationId, notification);
    }

    private static NotificationManagerCompat getNotificationManager(Context context) {
        if (sNotificationManager == null) {
            sNotificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        }
        return sNotificationManager;
    }

    private static boolean channelExists(Context context, String channelId) {
        return getNotificationManager(context).getNotificationChannel(channelId) != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createChannel(Context context, String channelId) {
        NotificationChannel channel;

        if (DOBIJECKA_CHANNEL_ID.equals(channelId)) {
            channel = new NotificationChannel(channelId,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
        } else {
            throw new RuntimeException("Unknown Notification Channel ID");
        }

        getNotificationManager(context).createNotificationChannel(channel);
    }

}
