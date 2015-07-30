package eu.zkkn.android.kaktus;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        String message = data.getString("message");
        Log.d(Config.TAG, "From: " + from + ", Message: " + message);
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
