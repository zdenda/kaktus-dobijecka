package eu.zkkn.android.kaktus;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

import eu.zkkn.android.kaktus.backend.registration.Registration;


/**
 * Service for registration to receiving Google Cloud Messages
 */
public class GcmRegistrationService extends IntentService {

    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    private static final String TAG = "GcmRegistrationService";
    private static final String[] TOPICS = {"global"};

    private static Registration registration = null;


    public GcmRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Initially this call goes out to the network to retrieve the token,
                // subsequent calls are local.
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(Config.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                Log.i(Config.TAG, "GCM Registration Token: " + token);

                sendRegistrationToServer(token);

                // Subscribe to topic channels, maybe in future
                //subscribeTopics(token);

                GcmHelper.saveGcmToken(this, token);
            }

        } catch (Exception e) {
            Log.d(Config.TAG, "Failed to complete GCM token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Preferences.setGcmSentTokenToServer(this, false);
        }

        // Notify UI that registration has completed.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(REGISTRATION_COMPLETE));

    }

    /**
     * Persist registration to App Engine backend.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) throws IOException {
        Log.d(Config.TAG, "Send GCM token to the backend server");
        if (registration == null) {
            Registration.Builder builder = new Registration.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(Config.BACKEND_ROOT_URL + "_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            registration = builder.build();
        }

        registration.register(token).execute();

    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
