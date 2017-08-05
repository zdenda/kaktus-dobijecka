package eu.zkkn.android.kaktus.fcm;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.LastNotification;
import eu.zkkn.android.kaktus.MainActivity;
import eu.zkkn.android.kaktus.R;


public class MyFcmListenerService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 1;

    public static final String FCM_MESSAGE_RECEIVED = "fcmMessageReceived";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        //Warning: App versions 0.4.6 (15) and bellow doesn't filter notifications nor support URI
        String type = data.get("type");
        if ("notification".equals(type)) {
            long sentTime = remoteMessage.getSentTime();
            String message = data.get("message");
            String uri = data.get("uri");

            Log.d(Config.TAG, "From: " + remoteMessage.getFrom() + ", Type: " + type + "Time: "
                    + sentTime + ", Message: " + message + ", URI: " + uri);

            // save it as the last notification
            LastNotification.save(this, new LastNotification.Notification(
                    new Date(sentTime), new Date(), message, uri));

            // Notify UI that a new FCM message was received.
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(FCM_MESSAGE_RECEIVED));

            // show notification if the message is fresh
            if (sentTime > (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12))) {
                showNotification(message, uri);
            }
        }

    }


    protected void showNotification(String message, String uri) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true);

        Intent intent = null;
        if (!TextUtils.isEmpty(uri)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        }
        // start MainActivity if URI is empty or intent can't be resolved
        if (intent == null || intent.resolveActivity(getPackageManager()) == null) {
            intent = new Intent(this, MainActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

}
