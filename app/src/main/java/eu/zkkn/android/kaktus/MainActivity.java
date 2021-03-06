package eu.zkkn.android.kaktus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Date;

import eu.zkkn.android.kaktus.fcm.FcmHelper;
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.fcm.SendTokenWorker;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mFcmRegistrationBroadcastReceiver;
    private BroadcastReceiver mFcmMessageBroadcastReceiver;

    private View mLastNotification;
    private TextView mTvLastNotificationDate;
    private TextView mTvLastNotificationText;
    private SemaphoreView mSemaphoreStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSemaphoreStatus = findViewById(R.id.tv_status);

        //TODO: create parent Play Services Activity
        if (checkPlayServices()) {

            if (TextUtils.isEmpty(FcmHelper.loadFcmToken(this))) {
                if (FcmHelper.missingSubscriptionToNotifications(this)) {
                    SendTokenWorker.runSendTokenTask(this);
                }
                mSemaphoreStatus.setInfo(R.string.status_fcm_registration_in_progress);
                // register local broadcast receiver for result of the registration.
                registerFcmRegistrationReceiver();
            } else {
                mSemaphoreStatus.setOk(R.string.status_fcm_registered);
            }

            // Display FCM Token after one short and one long click on status message
            final View statusLayout = findViewById(R.id.fl_status);
            statusLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            showFcmToken();
                            return true;
                        }
                    });
                }
            });

            SendTokenWorker.schedulePeriodicRefresh(this);

        } else {
            mSemaphoreStatus.setError(R.string.status_missing_google_play_services);
        }

        // Last notification
        mLastNotification = findViewById(R.id.cv_notification);
        mTvLastNotificationDate = findViewById(R.id.tv_lastNotificationDate);
        mTvLastNotificationText = findViewById(R.id.tv_lastNotificationText);
        refreshLastNotificationViews();
        registerFcmMessageReceiver();

        // Facebook
        if (!Preferences.isFacebookInfoHidden(this)) {
            findViewById(R.id.iv_hideFb).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.cv_facebook).setVisibility(View.GONE);
                    Preferences.setFacebookInfoHidden(MainActivity.this, true);
                }
            });
            findViewById(R.id.iv_fbIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewUri(MainActivity.this, Config.KAKTUS_FACEBOOK_URL);
                }
            });
            ((TextView) findViewById(R.id.tv_lastFbPostDate)).setText(Helper.formatDate(
                    MainActivity.this, new Date()));
        } else {
            findViewById(R.id.cv_facebook).setVisibility(View.GONE);
        }

        // Remote config
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.activate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    String errorMsg = remoteConfig.getString("error");
                    String infoMsg = remoteConfig.getString("info");
                    // Show error
                    if (!TextUtils.isEmpty(errorMsg)) {
                        ((TextView) findViewById(R.id.tv_infoText)).setText(errorMsg);
                        View info = findViewById(R.id.cv_info);
                        info.setBackgroundColor(
                                getResources().getColor(R.color.cardErrorBackground));
                        info.setVisibility(View.VISIBLE);
                    // Show info if there's no error which has bigger priority
                    } else if (!TextUtils.isEmpty(infoMsg)) {
                        ((TextView) findViewById(R.id.tv_infoText)).setText(infoMsg);
                        findViewById(R.id.cv_info).setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        remoteConfig.fetch();

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                            String.format("%s (%s)",
                                    Helper.formatDate(MainActivity.this, notification.received),
                                    notification.from
                            )
                    );
                    return true;
                }
            });
            mTvLastNotificationText.setText(notification.text);
            mLastNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewUri(MainActivity.this, notification.uri != null ?
                            notification.uri : Config.KAKTUS_DOBIJCECKA_URL);
                }
            });
        } else {
            mTvLastNotificationDate.setText(Helper.formatDate(this, new Date()));
            mTvLastNotificationText.setText(R.string.lastNotification_none);
            mLastNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewUri(MainActivity.this, Config.KAKTUS_DOBIJCECKA_URL);
                }
            });
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
                new IntentFilter(SendTokenWorker.REGISTRATION_COMPLETE));
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

    private void showFcmToken() {
        final String title = getString(R.string.dialog_fcm_token_title);
        final String token = FcmHelper.loadFcmToken(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(Helper.formatHtml(
                "%1$s <br/><br/>%2$s<br/><br/><small>* %3$s</small>",
                token,
                getString(R.string.dialog_fcm_token_refresh_time,
                        Preferences.getLastSubscriptionRefreshTime(this)),
                getString(R.string.dialog_fcm_token_warning))
        );
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
