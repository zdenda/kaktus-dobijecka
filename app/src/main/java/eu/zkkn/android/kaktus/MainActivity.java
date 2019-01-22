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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import eu.zkkn.android.kaktus.FacebookPostsRepository.FbPost;
import eu.zkkn.android.kaktus.fcm.FcmHelper;
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService;
import eu.zkkn.android.kaktus.fcm.SendTokenTaskService;

import static eu.zkkn.android.kaktus.FacebookPostsRepository.Data.Status;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mFcmRegistrationBroadcastReceiver;
    private BroadcastReceiver mFcmMessageBroadcastReceiver;
    private FirebaseAnalyticsHelper mFirebaseAnalytics;

    private MainViewModel mViewModel;

    private View mLastNotification;
    private TextView mTvLastNotificationDate;
    private TextView mTvLastNotificationText;
    private SemaphoreView mSemaphoreStatus;
    private View mFbImageFrame;
    private ImageView mIvFbImage;
    private TextView mTvFbPostDate;
    private TextView mTvFbPostText;
    private ViewSwitcher mVsFbRefresh;
    private TextView mTvFbError;
    private View mfbErrorDivider;
    private View mFbError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mFirebaseAnalytics = new FirebaseAnalyticsHelper(FirebaseAnalytics.getInstance(this));

        mSemaphoreStatus = findViewById(R.id.tv_status);

        //TODO: create parent Play Services Activity
        if (checkPlayServices()) {

            if (TextUtils.isEmpty(FcmHelper.loadFcmToken(this))) {
                if (FcmHelper.missingSubscriptionToNotifications(this)) {
                    SendTokenTaskService.runSendTokenTask(this);
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
        //TODO: if sync is disabled, show some info
        findViewById(R.id.iv_fbIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewUri(MainActivity.this, Config.KAKTUS_FACEBOOK_URL);
            }
        });
        findViewById(R.id.ib_fbPostRefresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAnalytics.logEvent(FirebaseAnalyticsHelper.EVENT_FB_REFRESH);
                //TODO: what if there's no internet connection
                if (mViewModel.isFbSyncEnabled()) {
                    mViewModel.refreshLastFacebookPost();
                } else {
                    showEnableFbSyncDialog();
                }
            }
        });
        mFbImageFrame = findViewById(R.id.fl_lastFbPostImage);
        mIvFbImage = findViewById(R.id.iv_lastFbPostImage);
        mTvFbPostText = findViewById(R.id.tv_lastFbPostText);
        mTvFbPostDate = findViewById(R.id.tv_lastFbPostDate);
        mVsFbRefresh = findViewById(R.id.vs_fbPostRefresh);
        mfbErrorDivider = findViewById(R.id.v_fbErrorDivider);
        mFbError = findViewById(R.id.fl_fbError);
        mTvFbError = findViewById(R.id.tv_fbError);

        mViewModel.getLastFacebookPost().observe(this, setupLastFacebookPostObserver());


        // if there's no settings for sync, enable it
        if (Preferences.getSyncStatus(this) == Preferences.SYNC_NOT_SET) {
            mViewModel.enableFbSync();
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
        menu.findItem(R.id.action_sync_settings).setChecked(mViewModel.isFbSyncEnabled());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_sync_settings:
                if (!item.isChecked()) {
                    mViewModel.enableFbSync();
                    mFirebaseAnalytics.logEvent(FirebaseAnalyticsHelper.EVENT_SYNC_ON);
                } else {
                    mViewModel.disableFbSync();
                    mFirebaseAnalytics.logEvent(FirebaseAnalyticsHelper.EVENT_SYNC_OFF);
                }
                invalidateOptionsMenu();
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private Observer<FacebookPostsRepository.Data> setupLastFacebookPostObserver() {
        return new Observer<FacebookPostsRepository.Data>() {

            @Override
            public void onChanged(FacebookPostsRepository.Data data) {
                Log.d("ZKLog", "onChanged(FbPost)");

                // Loading
                mVsFbRefresh.setDisplayedChild(Status.LOADING == data.getStatus() ? 1 : 0);

                // Error
                if (Status.ERROR == data.getStatus()) {
                    mTvFbError.setText(data.getMessage());
                    mfbErrorDivider.setVisibility(View.VISIBLE);
                    mFbError.setVisibility(View.VISIBLE);
                } else {
                    mfbErrorDivider.setVisibility(View.GONE);
                    mFbError.setVisibility(View.GONE);
                }

                // Empty
                if (Status.EMPTY == data.getStatus() || data.getData() == null) {
                    mTvFbPostDate.setText(Helper.formatDate(MainActivity.this, new Date()));
                    mTvFbPostText.setText(R.string.lastFbPost_none);

                    // Success
                } else {

                    FbPost fbPost = data.getData();
                    //TODO: add link to specific Facebook post ("permalink_url")
                    mTvFbPostDate.setText(Helper.formatDate(MainActivity.this, fbPost.getDate()));
                    //TODO: click on this TextView doesn't open the web browser
                    if (!TextUtils.isEmpty(fbPost.getText())) {
                        mTvFbPostText.setText(fbPost.getText());
                    } else {
                        mTvFbPostText.setVisibility(View.GONE);
                    }
                    if (fbPost.getImageUrl() != null) {
                        Picasso.with(MainActivity.this).load(fbPost.getImageUrl()).into(mIvFbImage,
                                new Helper.BackgroundColorCallback(mIvFbImage, mFbImageFrame));
                    } else {
                        mFbImageFrame.setVisibility(View.GONE);
                    }

                }

            }
        };
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

    private void showEnableFbSyncDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_sync_title);
        builder.setMessage(R.string.dialog_sync_message);
        builder.setPositiveButton(R.string.generic_alert_button_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.enableFbSync();
                        //refresh sync checkbox in the menu
                        invalidateOptionsMenu();
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
