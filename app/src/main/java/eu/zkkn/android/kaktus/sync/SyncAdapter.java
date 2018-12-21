package eu.zkkn.android.kaktus.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ClassInfo;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.Helper;
import eu.zkkn.android.kaktus.LastFbPost;
import eu.zkkn.android.kaktus.LastFbPost.FbPost;
import eu.zkkn.android.kaktus.model.FbApiPost;
import eu.zkkn.android.kaktus.model.FbApiResponse;


/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String FB_SYNC_FINISHED = "fbSyncFinished";

    private static final String GRAPH_API_VERSION = "v3.0";
    private static final String FB_PAGE_NAME = "Kaktus";


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        init();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init();
    }

    private void init() {
        Log.d(Config.TAG, "SyncAdapter.init()");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(Config.TAG, "SyncAdapter.onPerformSync()");

        try {

            HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport()
                    .createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest httpRequest) throws IOException {
                            //TODO: better error handling for FB API requests
                            // httpRequest.setThrowExceptionOnExecuteError(false);
                            httpRequest.setParser(new JsonObjectParser(new AndroidJsonFactory()));
                        }
                    });

            GenericUrl url = new GenericUrl("https://graph.facebook.com/"
                    + GRAPH_API_VERSION + "/" + FB_PAGE_NAME + "/posts");
            // limit Fb API field to only those field names which are in FbApiPost class,
            url.put("fields", TextUtils.join(",", ClassInfo.of(FbApiPost.class).getNames()));
            url.put("access_token", Config.FB_ACCESS_TOKEN);
            url.put("limit", 1);

            HttpResponse response = requestFactory.buildGetRequest(url).execute();
            FbApiResponse fbApiResponse = response.parseAs(FbApiResponse.class);
            FbApiPost fbApiPost = fbApiResponse.posts[0];
            FbPost fbPost = new FbPost(Helper.parseFbDate(fbApiPost.createdTime), fbApiPost.message,
                    Helper.imageUrlFromFbPostAttachment(fbApiPost));
            //TODO: use ContentProvider
            LastFbPost.save(getContext(), fbPost);

            // Notify UI that a new FCM message was received.
            LocalBroadcastManager.getInstance(getContext())
                    .sendBroadcast(new Intent(FB_SYNC_FINISHED));

        } catch (IOException e) {
            Log.e("SyncAdapter", e.getMessage() != null ? e.getMessage() : "IOException");
        }

        Log.d(Config.TAG, "SyncAdapter.onPerformSync() end");
    }
}
