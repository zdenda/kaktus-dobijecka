package eu.zkkn.android.kaktus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;

import java.util.Date;

import eu.zkkn.android.kaktus.fcm.FcmHelper;
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.fcm.SendTokenTaskService;
import eu.zkkn.android.kaktus.sync.SyncAdapter;
import eu.zkkn.android.kaktus.sync.SyncUtils;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mFcmRegistrationBroadcastReceiver;
    private BroadcastReceiver mFcmMessageBroadcastReceiver;
    private BroadcastReceiver mFbSyncBroadcastReceiver;
    private TextView mTvLastNotificationDate;
    private TextView mTvLastNotificationText;
    private SemaphoreView mSemaphoreStatus;
    private View mFbImageFrame;
    private ImageView mIvFbImage;
    private TextView mTvFbPostDate;
    private TextView mTvFbPostText;
    private ViewSwitcher mVsFbRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSemaphoreStatus = (SemaphoreView) findViewById(R.id.tv_status);

        //TODO: create parent Play Services Activity
        if (checkPlayServices()) {

            if (TextUtils.isEmpty(FcmHelper.loadFcmToken(this))) {
                mSemaphoreStatus.setInfo(R.string.status_fcm_registration_in_progress);
                // register local broadcast receiver for result of the registration.
                registerFcmRegistrationReceiver();
            } else {
                mSemaphoreStatus.setOk(R.string.status_fcm_registered);
            }

            // Display FCM Token after one short and one long click on status message
            mSemaphoreStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSemaphoreStatus.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            showFcmToken();
                            return true;
                        }
                    });
                }
            });

        } else {
            mSemaphoreStatus.setError(R.string.status_missing_google_play_services);
        }

        // Last notification
        mTvLastNotificationDate = (TextView) findViewById(R.id.tv_lastNotificationDate);
        mTvLastNotificationText = (TextView) findViewById(R.id.tv_lastNotificationText);
        refreshLastNotificationViews();
        registerFcmMessageReceiver();

        // Facebook
        //TODO: if sync is disabled, show some info
        mFbImageFrame = findViewById(R.id.fl_lastFbPostImage);
        mIvFbImage = (ImageView) findViewById(R.id.iv_lastFbPostImage);
        mTvFbPostText = (TextView) findViewById(R.id.tv_lastFbPostText);
        mTvFbPostDate = (TextView) findViewById(R.id.tv_lastFbPostDate);
        mVsFbRefresh = (ViewSwitcher) findViewById(R.id.vs_fbPostRefresh);
        findViewById(R.id.ib_fbPostRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFbSyncEnabled()) {
                    forceFbSync();
                } else {
                    showEnableFbSyncDialog();
                }
            }
        });

        refreshFbPostViews();
        registerFbSyncReceiver();

        // if there's no settings for sync, enable it
        if (Preferences.getSyncStatus(this) == Preferences.SYNC_NOT_SET) {
            SyncUtils.enableSync(this);
            // and start synchronization on the first run
            if (Preferences.isFirst(this)) {
                forceFbSync();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterFcmRegistrationReceiver();
        unregisterFcmMessageReceiver();
        unregisterFbSyncReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_sync_settings).setChecked(isFbSyncEnabled());
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


    private boolean isFbSyncEnabled() {
        return Preferences.getSyncStatus(this) == Preferences.SYNC_ENABLED
                && SyncUtils.isSyncable(this); //account could have been removed in system settings
    }

    private void forceFbSync() {
        mVsFbRefresh.setDisplayedChild(1);
        SyncUtils.startSync(this);
    }

    private void refreshLastNotificationViews() {
        final LastNotification.Notification notification = LastNotification.load(this);
        if (notification != null) {
            mTvLastNotificationDate.setText(Helper.formatDate(this, notification.sent));
            // long click on the sent date to show the received date
            mTvLastNotificationDate.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Helper.showAlert(MainActivity.this,
                            getString(R.string.dialog_notification_received_title),
                            Helper.formatDate(MainActivity.this, notification.received));
                    return true;
                }
            });
            mTvLastNotificationText.setText(notification.text);
        } else {
            mTvLastNotificationDate.setText(Helper.formatDate(this, new Date()));
            mTvLastNotificationText.setText(R.string.lastNotification_none);
        }
    }

    private void refreshFbPostViews() {
        LastFbPost.FbPost fbPost = LastFbPost.load(this);
        mVsFbRefresh.setDisplayedChild(0);
        if (fbPost != null) {
            //TODO: add link to Facebook ("permalink_url")
            mTvFbPostDate.setText(Helper.formatDate(this, fbPost.date));
            mTvFbPostText.setText(fbPost.text);
            if (fbPost.imageUrl != null) {
                Picasso.with(this).load(fbPost.imageUrl).into(mIvFbImage,
                        new Helper.BackgroundColorCallback(mIvFbImage, mFbImageFrame));
            } else {
                mFbImageFrame.setVisibility(View.GONE);
            }
        } else {
            mTvFbPostDate.setText(Helper.formatDate(this, new Date()));
            mTvFbPostText.setText(R.string.lastFbPost_none);
        }
    }

    private void registerFcmRegistrationReceiver() {
        mFcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.isEmpty(FcmHelper.loadFcmToken(context))) {
                    mSemaphoreStatus.setError(R.string.status_fcm_registration_error);
                } else {
                    mSemaphoreStatus.setOk(R.string.status_fcm_registered);
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
                refreshLastNotificationViews();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mFcmMessageBroadcastReceiver,
                new IntentFilter(MyFcmListenerService.FCM_MESSAGE_RECEIVED));
    }

    private void unregisterFcmMessageReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFcmMessageBroadcastReceiver);
    }

    private void registerFbSyncReceiver() {
        mFbSyncBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshFbPostViews();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mFbSyncBroadcastReceiver,
                new IntentFilter(SyncAdapter.FB_SYNC_FINISHED));
    }

    private void unregisterFbSyncReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFbSyncBroadcastReceiver);
    }

    private void showEnableFbSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_sync_title);
        builder.setMessage(R.string.dialog_sync_message);
        builder.setPositiveButton(R.string.generic_alert_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SyncUtils.enableSync(MainActivity.this);
                        invalidateOptionsMenu(); //refresh sync checkbox in the menu
                        forceFbSync();
                    }
                }
        );
        builder.setNegativeButton(R.string.generic_alert_button_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        builder.show();
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
