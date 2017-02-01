package eu.zkkn.android.kaktus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Date;

import eu.zkkn.android.kaktus.fcm.FcmHelper;
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.fcm.SendTokenTaskService;
import eu.zkkn.android.kaktus.sync.SyncUtils;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mFcmRegistrationBroadcastReceiver;
    private BroadcastReceiver mFcmMessageBroadcastReceiver;
    private TextView mTvStatus;
    private TextView mTvLastNotificationDate;
    private TextView mTvLastNotificationText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvStatus = (TextView) findViewById(R.id.tv_status);

        if (checkPlayServices()) {

            if (TextUtils.isEmpty(FcmHelper.loadFcmToken(this))) {
                mTvStatus.setText(R.string.status_fcm_registration_in_progress);
                // register local broadcast receiver for result of the registration.
                registerFcmRegistrationReceiver();
            } else {
                mTvStatus.setText(R.string.status_fcm_registered);
            }

            // Display FCM Token after one short and one long click on status message
            mTvStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTvStatus.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            showFcmToken();
                            return true;
                        }
                    });
                }
            });

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
        TextView lastFbPostDate = (TextView) findViewById(R.id.tv_lastFbPostDate);
        TextView lastFbPostText = (TextView) findViewById(R.id.tv_lastFbPostText);
        if (fbPost != null) {
            lastFbPostDate.setText(Helper.formatDate(this, fbPost.date));
            lastFbPostText.setText(fbPost.text);
        } else {
            lastFbPostDate.setText(Helper.formatDate(this, new Date()));
            lastFbPostText.setText(R.string.lastFbPost_none);
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
                new IntentFilter(SendTokenTaskService.REGISTRATION_COMPLETE));
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

    private void showFcmToken() {
        final String title = getString(R.string.dialog_fcm_token_title);
        final String token = FcmHelper.loadFcmToken(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(Helper.formatHtml("%1$s <br/><br/><small>* %2$s</small>",
                token, getString(R.string.dialog_fcm_token_warning)));
        builder.setNeutralButton(R.string.dialog_fcm_token_button_copy_to_clipboard,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helper.copyToClipboard(MainActivity.this, title, token);
                    }
                }
        );
        builder.setPositiveButton(R.string.dialog_fcm_token_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        builder.show();
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
