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
        public @Nullable String uri;

        public Notification(Date sent, Date received, String text, @Nullable String uri) {
            this.sent = sent;
            this.received = received;
            this.text = text;
            this.uri = uri;
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
