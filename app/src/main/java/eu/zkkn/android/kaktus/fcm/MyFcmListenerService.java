package eu.zkkn.android.kaktus.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.LastNotification;
import eu.zkkn.android.kaktus.Preferences;
import eu.zkkn.android.kaktus.R;


public class MyFcmListenerService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 1;

    public static final String FCM_MESSAGE_RECEIVED = "fcmMessageReceived";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        String message = data.get("message");
        Log.d(Config.TAG, "From: " + from + ", Message: " + message);
        // save it as the last notification
        Preferences.setLastNotification(this,
                new LastNotification.Notification(new Date(), message));
        // Notify UI that a new FCM message was received.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(FCM_MESSAGE_RECEIVED));
        showNotification(message);
    }

    protected void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true);

        Intent kaktusWeb = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.mujkaktus.cz/chces-pridat"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, kaktusWeb,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, builder.build());
    }

}