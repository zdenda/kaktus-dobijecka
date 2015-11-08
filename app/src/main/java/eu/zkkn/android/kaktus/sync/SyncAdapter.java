package eu.zkkn.android.kaktus.sync;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import eu.zkkn.android.kaktus.Config;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

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
        //TODO: add synchronization which actually synchronizes something
        SystemClock.sleep(5 * 1000);
        Log.d(Config.TAG, "SyncAdapter.onPerformSync() end");
    }
}
