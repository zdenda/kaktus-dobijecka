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
import com.google.android.gms.common.GooglePlayServicesUtil;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mGcmRegistrationBroadcastReceiver;
    private TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvStatus = (TextView) findViewById(R.id.tv_status);

        if (checkPlayServices()) {
            if (TextUtils.isEmpty(GcmHelper.loadGcmToken(this))) {
                mTvStatus.setText(R.string.status_gcm_registration_in_progress);
                // Start IntentService to register this application with GCM and
                // register local broadcast receiver for result of the registration.
                registerGcmRegistrationReceiver();
                startService(new Intent(this, GcmRegistrationService.class));
            } else {
                mTvStatus.setText(R.string.status_gcm_registered);
            }
        } else {
            mTvStatus.setTextColor(Color.RED);
            mTvStatus.setText(R.string.status_missing_google_play_services);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterGcmRegistrationReceiver();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerGcmRegistrationReceiver() {
        mGcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.isEmpty(GcmHelper.loadGcmToken(context))) {
                    mTvStatus.setTextColor(Color.RED);
                    mTvStatus.setText(R.string.status_gcm_registration_error);
                } else {
                    mTvStatus.setText(R.string.status_gcm_registered);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mGcmRegistrationBroadcastReceiver,
                new IntentFilter(GcmRegistrationService.REGISTRATION_COMPLETE));
    }

    private void unRegisterGcmRegistrationReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGcmRegistrationBroadcastReceiver);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                Log.i(Config.TAG, "This device is not supported. GooglePlayServices not available.");
                finish();
            }
            return false;
        }
        return true;
    }

}
