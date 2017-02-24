package eu.zkkn.android.kaktus;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 *
 */
public class LastNotification {

    public static class Notification {

        public Date sent;
        public Date received;
        public String text;

        public Notification(Date sent, Date received, String text) {
            this.sent = sent;
            this.received = received;
            this.text = text;
        }

    }

    public static void save(Context context, Notification notification) {
        Preferences.setLastNotification(context, notification);
    }


    @Nullable
    public static Notification load(Context context) {
        return Preferences.getLastNotification(context);
    }

}
