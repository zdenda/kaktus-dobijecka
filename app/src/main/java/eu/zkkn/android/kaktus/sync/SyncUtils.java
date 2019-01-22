package eu.zkkn.android.kaktus.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.Helper;
import eu.zkkn.android.kaktus.Preferences;
import eu.zkkn.android.kaktus.R;


/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {

    /**
     * Enable Synchronization
     * Creates account for synchronization (if doesn't exist already) and sets periodic synchronization
     * @param context Context
     */
    public static void enableSync(Context context) {
        Log.d(Config.TAG, "Enable synchronization");
        createSyncAccount(context);
        setPeriodicSync(context, Helper.DAY_IN_S);
        Preferences.setSyncStatus(context, Preferences.SYNC_ENABLED);
    }

    /**
     * Disable synchronization
     * Remove periodic synchronization and delete account for synchronization
     * @param context Context
     */
    public static void disableSync(Context context) {
        Log.d(Config.TAG, "Disable synchronization");
        removePeriodicSync(context);
        removeSyncAccount(context);
        Preferences.setSyncStatus(context, Preferences.SYNC_DISABLED);
    }

    public static boolean isSyncEnabled(Context context) {
        return Preferences.getSyncStatus(context) == Preferences.SYNC_ENABLED
                && SyncUtils.isSyncable(context); //account could have been removed in system settings
    }

    /**
     * Manually start synchronization
     * @param context Context
     */
    public static void startSync(Context context) {
        ContentResolver.requestSync(getAccount(context),
                context.getString(R.string.provider),
                new Bundle());
    }

    public static boolean isSyncActive(Context context) {
        Account account = getAccount(context);
        String authority = getAuthority(context);
        return ContentResolver.isSyncActive(account, authority)
                || ContentResolver.isSyncPending(account, authority);
    }


    /**
     * Create a new dummy account for the sync adapter
     * @param context Context
     */
    private static void createSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Add the account and account type, no password or user data
        if (!accountManager.addAccountExplicitly(getAccount(context), null, null)) {
            Log.d(Config.TAG, "The account exists or some other error occurred.");
        }
    }

    /**
     * Remove the dummy account for the sync adapter
     * @param context Context
     */
    private static void removeSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // AccountManager.removeAccountExplicitly() is available only since SDK 22
        // so keep the account on older SDKs (it shouldn't be a problem)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccountExplicitly(getAccount(context));
        }
    }

    /**
     * Schedules periodic synchronization
     * @param context Context
     * @param interval how frequently the sync should be performed, in seconds
     */
    private static void setPeriodicSync(Context context, long interval) {
        Account account = getAccount(context);
        String authority = context.getString(R.string.provider);

        // Inform the system that this account supports sync
        ContentResolver.setIsSyncable(account, authority, 1);
        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically(account, authority, true);
        // Add a schedule for automatic synchronization
        ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY, interval);
    }

    /**
     * Remove periodic synchronization
     * @param context Context
     */
    private static void removePeriodicSync(Context context) {
        Account account = getAccount(context);
        String authority = context.getString(R.string.provider);

        ContentResolver.removePeriodicSync(account, authority, Bundle.EMPTY);
        ContentResolver.setSyncAutomatically(account, authority, false);
        // keep syncable, so the sync can be triggered from android settings
        //ContentResolver.setIsSyncable(account, authority, 0);
    }

    private static boolean isSyncable(Context context) {
        return 0 < ContentResolver.getIsSyncable(getAccount(context), context.getString(R.string.provider));
    }

    @NonNull
    private static Account getAccount(Context context) {
        return new Account("Kaktus (Facebook)", context.getString(R.string.account_type));
    }

    @NonNull
    private static String getAuthority(Context context) {
        return context.getString(R.string.provider);
    }

}
