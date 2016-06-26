package eu.zkkn.android.kaktus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Date;

import eu.zkkn.android.kaktus.fcm.FcmHelper;
import eu.zkkn.android.kaktus.fcm.IdListenerService;
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.sync.SyncUtils;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mFcmRegistrationBroadcastReceiver;
    private BroadcastReceiver mFcmMessageBroadcastReceiver;
    private TextView mTvStatus;
    private TextView mTvLastNotificationDate;
    private TextView mTvLastNotificationText;
    private TextView mTvLastFbPostDate;
    private TextView mTvLastFbPostText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvStatus = (TextView) findViewById(R.id.tv_status);

        if (checkPlayServices()) {
            if (TextUtils.isEmpty(FcmHelper.loadFcmToken(this))) {
                // register local broadcast receiver for result of the registration.
                registerFcmRegistrationReceiver();
                mTvStatus.setText(R.string.status_fcm_registration_in_progress);
            } else {
                mTvStatus.setText(R.string.status_fcm_registered);
            }
        } else {
            mTvStatus.setTextColor(Color.RED);
            mTvStatus.setText(R.string.status_missing_google_play_services);
        }

        // Last notification
        LastNotification.Notification notification = LastNotification.load(this);
        mTvLastNotificationDate = (TextView) findViewById(R.id.tv_lastNotificationDate);
        mTvLastNotificationText = (TextView) findViewById(R.id.tv_lastNotificationText);
        registerFcmMessageReceiver();

        if (notification != null) {
            mTvLastNotificationDate.setText(Helper.formatDate(this, notification.date));
            mTvLastNotificationText.setText(notification.text);
        } else {
            mTvLastNotificationDate.setText(Helper.formatDate(this, new Date()));
            mTvLastNotificationText.setText(R.string.lastNotification_none);
        }

        // if there's no settings for sync, enable it
        if (Preferences.getSyncStatus(this) == Preferences.SYNC_NOT_SET) {
            SyncUtils.enableSync(this);
        }

        // Facebook
        //TODO: update on synchronization
        LastFbPost.FbPost fbPost = LastFbPost.load(this);
        mTvLastFbPostDate = (TextView) findViewById(R.id.tv_lastFbPostDate);
        mTvLastFbPostText = (TextView) findViewById(R.id.tv_lastFbPostText);
        if (fbPost != null) {
            mTvLastFbPostDate.setText(Helper.formatDate(this, fbPost.date));
            mTvLastFbPostText.setText(fbPost.text);
        } else {
            mTvLastFbPostDate.setText(Helper.formatDate(this, new Date()));
            mTvLastFbPostText.setText(R.string.lastFbPost_none);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterFcmRegistrationReceiver();
        unregisterFcmMessageReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_sync_settings)
                .setChecked(Preferences.getSyncStatus(this) == Preferences.SYNC_ENABLED
                        && SyncUtils.isSyncable(this)); //account could have been removed in system settings
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sync_settings) {
            if (!item.isChecked()) {
                SyncUtils.enableSync(this);
            } else {
                SyncUtils.disableSync(this);
            }
            invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerFcmRegistrationReceiver() {
        mFcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.isEmpty(FcmHelper.loadFcmToken(context))) {
                    mTvStatus.setTextColor(Color.RED);
                    mTvStatus.setText(R.string.status_fcm_registration_error);
                } else {
                    mTvStatus.setText(R.string.status_fcm_registered);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mFcmRegistrationBroadcastReceiver,
                new IntentFilter(IdListenerService.REGISTRATION_COMPLETE));
    }

    private void unRegisterFcmRegistrationReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFcmRegistrationBroadcastReceiver);
    }

    private void registerFcmMessageReceiver() {
        mFcmMessageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LastNotification.Notification notification = LastNotification.load(context);
                if (notification != null) {
                    mTvLastNotificationDate.setText(Helper.formatDate(context, notification.date));
                    mTvLastNotificationText.setText(notification.text);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mFcmMessageBroadcastReceiver,
                new IntentFilter(MyFcmListenerService.FCM_MESSAGE_RECEIVED));
    }

    private void unregisterFcmMessageReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFcmMessageBroadcastReceiver);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                Log.i(Config.TAG, "This device is not supported. GooglePlayServices not available.");
                finish();
            }
            return false;
        }
        return true;
    }

}
