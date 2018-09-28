package eu.zkkn.android.kaktus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

//TODO add licences (needs play services v11.2.0)
// https://developers.google.com/android/guides/opensource
// res/raw/third_party_licenses

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_about);

        final FirebaseAnalyticsHelper firebaseAnalytics = new FirebaseAnalyticsHelper(
                FirebaseAnalytics.getInstance(this));

        findViewById(R.id.ib_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((TextView) findViewById(R.id.tv_version)).setText(
                getString(R.string.about_version, BuildConfig.VERSION_NAME));
        ((TextView) findViewById(R.id.tv_privacy_policy_link)).setMovementMethod(
                LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.tv_sources_link)).setMovementMethod(
                LinkMovementMethod.getInstance());

        // donations
        final View donation = findViewById(R.id.ll_donation);
        String number = String.format("%s\u00A0%s\u00A0%s", Config.DONATION_NUMBER.substring(0, 3),
                Config.DONATION_NUMBER.substring(3, 6), Config.DONATION_NUMBER.substring(6, 9));
        ((TextView) findViewById(R.id.tv_donationText)).setText(
                getString(R.string.donation_text, number));
        final Intent kaktusAppIntent = Helper.getAppIntent(this, Config.KAKTUS_APP_ID);
        findViewById(R.id.bt_donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAnalytics.logEvent(FirebaseAnalyticsHelper.EVENT_DONATE_ABOUT);
                Helper.copyToClipboard(AboutActivity.this, Config.DONATION_NUMBER);
                startActivity(kaktusAppIntent);

            }
        });
        // hide donation box if any notification has ben received yet,
        // or the official Kaktus app is not installed
        if (LastNotification.load(this) == null || kaktusAppIntent == null) {
            donation.setVisibility(View.GONE);
        }
    }

}
