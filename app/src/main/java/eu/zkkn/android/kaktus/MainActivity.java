package eu.zkkn.android.kaktus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.content.PackageManagerCompat.UnusedAppRestrictionsStatus;
import androidx.core.content.UnusedAppRestrictionsConstants;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Date;
import java.util.Objects;

import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.fcm.FcmSubscriptionWorker;


public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Void> mAppSettingsLauncher = registerForActivityResult(
            new ActivityResultContract<Void, ActivityResult>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Void input) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                    return intent;
                }

                @Override
                public ActivityResult parseResult(int resultCode, @Nullable Intent intent) {
                    return new ActivityResult(resultCode, intent);
                }
            },
            result -> refreshAppNotificationsView()
    );

    private final ActivityResultLauncher<Void> mManageUnusedAppRestrictionsLauncher =
            registerForActivityResult(
                    new ActivityResultContract<Void, ActivityResult>() {
                        @NonNull
                        @Override
                        public Intent createIntent(@NonNull Context context, @Nullable Void input) {
                            return IntentCompat.createManageUnusedAppRestrictionsIntent(
                                    context, BuildConfig.APPLICATION_ID);
                        }

                        @Override
                        public ActivityResult parseResult(int resultCode, @Nullable Intent intent) {
                            return new ActivityResult(resultCode, intent);
                        }
                    },
                    result -> refreshAppHibernationView()

            );

    private final ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    result -> refreshAppNotificationsView()
            );


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

        requireNotificationPermission();

        //TODO: create parent Play Services Activity
        if (checkPlayServices()) {

            if (!Preferences.isSubscribedToNotifications(this)) {
                mSemaphoreStatus.setInfo(R.string.status_fcm_registration_in_progress);
                // register local broadcast receiver for result of the registration.
                registerFcmRegistrationReceiver();
                FcmSubscriptionWorker.runSubscribeToTopics(this);
            } else {
                mSemaphoreStatus.setOk(R.string.status_fcm_registered);
            }

            // Display FCM Token after one short and one long click on status message
            final View statusLayout = findViewById(R.id.fl_status);
            statusLayout.setOnClickListener(v -> statusLayout.setOnLongClickListener(v1 -> {
                showDebugInfo();
                return true;
            }));

            FcmSubscriptionWorker.schedulePeriodicSubscriptionRefresh(this);

        } else {
            mSemaphoreStatus.setError(R.string.status_missing_google_play_services);
        }

        // Last notification
        mLastNotification = findViewById(R.id.cv_notification);
        mTvLastNotificationDate = findViewById(R.id.tv_lastNotificationDate);
        mTvLastNotificationText = findViewById(R.id.tv_lastNotificationText);
        refreshLastNotificationViews();
        registerFcmMessageReceiver();

        // Remote config
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.activate().addOnCompleteListener(task -> {
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
        });

        remoteConfig.fetch();

        // App Notifications Disabled
        findViewById(R.id.bt_app_settings).setOnClickListener(
                view -> mAppSettingsLauncher.launch(null)
        );

        // App Hibernation
        findViewById(R.id.bt_app_hibernation).setOnClickListener(v ->
                mManageUnusedAppRestrictionsLauncher.launch(null)
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAppNotificationsView();
        refreshAppHibernationView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requireNotificationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterFcmRegistrationReceiver();
        unregisterFcmMessageReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
            mTvLastNotificationDate.setOnLongClickListener(v -> {
                Helper.showAlert(MainActivity.this,
                        getString(R.string.dialog_notification_received_title),
                        String.format("%s (%s)",
                                Helper.formatDate(MainActivity.this, notification.received),
                                notification.from
                        )
                );
                return true;
            });
            mTvLastNotificationText.setText(notification.text);
            mLastNotification.setOnClickListener(v ->
                    Helper.viewUri(MainActivity.this, notification.uri != null ?
                    notification.uri : Config.KAKTUS_DOBIJCECKA_URL)
            );
        } else {
            mTvLastNotificationDate.setText(Helper.formatDate(this, new Date()));
            mTvLastNotificationText.setText(R.string.lastNotification_none);
            mLastNotification.setOnClickListener(v ->
                    Helper.viewUri(MainActivity.this, Config.KAKTUS_DOBIJCECKA_URL)
            );
        }
    }

    private void refreshAppNotificationsView() {
        findViewById(R.id.cv_app_notifications).setVisibility(
                NotificationHelper.areNotificationsEnabled(this) ? View.GONE : View.VISIBLE
        );
    }

    private void refreshAppHibernationView() {
        Futures.addCallback(
                PackageManagerCompat.getUnusedAppRestrictionsStatus(this),
                new FutureCallback<Integer>() {
                    @Override
                    public void onSuccess(@UnusedAppRestrictionsStatus Integer result) {
                        switch (result) {
                            case UnusedAppRestrictionsConstants.ERROR:
                            case UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE:
                            case UnusedAppRestrictionsConstants.DISABLED:
                            case UnusedAppRestrictionsConstants.API_30_BACKPORT:
                            case UnusedAppRestrictionsConstants.API_30:
                                // App hibernation is NOT active
                                findViewById(R.id.cv_app_hibernation).setVisibility(View.GONE);
                                break;
                            case UnusedAppRestrictionsConstants.API_31:
                                // App hibernation is active
                                findViewById(R.id.cv_app_hibernation).setVisibility(View.VISIBLE);
                                break;
                            default:
                                findViewById(R.id.cv_app_hibernation).setVisibility(View.GONE);
                                Log.e(Config.TAG, "Unknown value for UnusedAppRestrictionsConstants: " + result);
                                break;
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        // Do nothing
                    }
                },
                ContextCompat.getMainExecutor(this)
        );
    }

    private void registerFcmRegistrationReceiver() {
        mFcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!Preferences.isSubscribedToNotifications(context)) {
                    mSemaphoreStatus.setError(R.string.status_fcm_registration_error);
                } else {
                    NotificationHelper.createChannel(MainActivity.this);
                    mSemaphoreStatus.setOk(R.string.status_fcm_registered);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mFcmRegistrationBroadcastReceiver,
                new IntentFilter(FcmSubscriptionWorker.WORKER_HAS_FINISHED));
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

    private void showDebugInfo() {
        new DebugInfoDialog(this).show();
    }

    private void requireNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return; // Only Android 13 and up
        String permission = Manifest.permission.POST_NOTIFICATIONS;
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_notifications_permission_title);
                builder.setMessage(R.string.dialog_notifications_permission_message);
                builder.setPositiveButton(R.string.dialog_notifications_permission_ok,
                        (dialog, which) -> mRequestPermissionLauncher.launch(permission));
                builder.show();
            } else {
                mRequestPermissionLauncher.launch(permission);
            }
        }
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
                Objects.requireNonNull(
                        apiAvailability.getErrorDialog(this, resultCode, 9000)
                ).show();
            } else {
                Log.i(Config.TAG, "This device is not supported. GooglePlayServices not available.");
                finish();
            }
            return false;
        }
        return true;
    }

}
