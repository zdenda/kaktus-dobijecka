package eu.zkkn.android.kaktus.fcm;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.LastNotification;
import eu.zkkn.android.kaktus.MainActivity;
import eu.zkkn.android.kaktus.NotificationHelper;
import eu.zkkn.android.kaktus.R;


public class MyFcmListenerService extends FirebaseMessagingService {

    public static final String FCM_MESSAGE_RECEIVED = "fcmMessageReceived";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //TODO: with the new Task Queue sender the same message can be in some rare circumstances sent multiple times
        Map<String, String> data = remoteMessage.getData();
        //TODO: log receiving of message to Firebase analytics
        //Warning: App versions 0.4.6 (15) and bellow doesn't filter notifications nor support URI
        String type = data.get("type");
        if ("notification".equals(type)) {
            long sentTime = remoteMessage.getSentTime();
            String from = remoteMessage.getFrom();
            String message = data.get("message");
            String uri = data.get("uri");

            Log.d(Config.TAG, "From: " + from + ", Type: " + type + "Time: "
                    + sentTime + ", Message: " + message + ", URI: " + uri);

            // save it as the last notification
            LastNotification.save(this, new LastNotification.Notification(
                    new Date(sentTime), new Date(), message, uri, from));

            // Notify UI that a new FCM message was received.
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(FCM_MESSAGE_RECEIVED));

            // show notification if the message is fresh
            if (sentTime > (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12))) {
                showNotification(message, uri);
            }
        }

    }


    protected void showNotification(String message, @Nullable String uri) {
        NotificationCompat.Builder builder = NotificationHelper
                .getDefaultBuilder(this, NotificationHelper.DOBIJECKA_CHANNEL_ID)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // add action if URI is not empty and intent for that URI can be resolved
        Intent action = null;
        if (!TextUtils.isEmpty(uri)) {
            action = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        }
        if (action != null && action.resolveActivity(getPackageManager()) != null) {
            builder.addAction(R.drawable.ic_open, getString(R.string.notification_action_view),
                    PendingIntent.getActivity(this, 0, action, 0));
        }

        NotificationHelper.notify(this, NotificationHelper.DOBIJECKA_NOTIFICATION_ID,
                builder.build());
    }

}
