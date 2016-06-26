package eu.zkkn.android.kaktus.fcm;

import android.content.Intent;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.Helper;
import eu.zkkn.android.kaktus.Preferences;
import eu.zkkn.android.kaktus.backend.registration.Registration;


public class IdListenerService extends FirebaseInstanceIdService {

    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    private static Registration registration = null;


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {

        try {
            // Get updated InstanceID token.
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.i(Config.TAG, "FCM Registration Token: " + token);

            sendRegistrationToServer(token);

            FcmHelper.saveFcmToken(this, token);

        } catch (Exception e) {
            Log.d(Config.TAG, "Failed to complete FCM token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Preferences.setFcmSentTokenToServer(this, false);
        }

        // Notify UI that registration has completed.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(REGISTRATION_COMPLETE));
    }


    /**
     * Persist registration to App Engine backend.
     *
     * @param token The new token.
     */
    @WorkerThread
    private void sendRegistrationToServer(String token) throws IOException {
        Log.d(Config.TAG, "Send FCM token to the backend server");
        if (registration == null) {
            Registration.Builder builder = new Registration.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(Config.BACKEND_ROOT_URL + "_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true)
                                    .getRequestHeaders().setUserAgent(Helper.getUserAgent());
                        }
                    });
            registration = builder.build();
        }

        registration.register(token).execute();

    }

}
