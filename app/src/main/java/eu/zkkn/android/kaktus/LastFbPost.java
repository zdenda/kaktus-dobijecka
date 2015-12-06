package eu.zkkn.android.kaktus;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 *
 */
public class LastFbPost {

    public static class FbPost {

        public Date date;
        public String text;

        public FbPost(Date date, String text) {
            this.date = date;
            this.text = text;
        }

    }

    public static void save(Context context, FbPost fbPost) {
        Preferences.setLastFbPost(context, fbPost);
    }


    @Nullable
    public static FbPost load(Context context) {
        return Preferences.getLastFbPost(context);
    }

}
