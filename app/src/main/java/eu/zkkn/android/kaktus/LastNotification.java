package eu.zkkn.android.kaktus;

import android.content.Context;

import java.util.Date;

import androidx.annotation.Nullable;


/**
 *
 */
public class LastNotification {

    public static class Notification {

        public Date sent;
        public Date received;
        public String text;
        public @Nullable String uri;
        public @Nullable String from;

        public Notification(Date sent, Date received, String text, @Nullable String uri,
                            @Nullable String from) {
            this.sent = sent;
            this.received = received;
            this.text = text;
            this.uri = uri;
            this.from = from;
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
