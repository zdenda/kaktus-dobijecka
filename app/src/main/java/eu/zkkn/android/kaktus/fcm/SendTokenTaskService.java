package eu.zkkn.android.kaktus.fcm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import eu.zkkn.android.kaktus.Config;
import eu.zkkn.android.kaktus.Helper;
import eu.zkkn.android.kaktus.Preferences;
import eu.zkkn.android.kaktus.backend.registration.Registration;


public class SendTokenTaskService extends GcmTaskService {

    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    private static final String FCM_TOPIC_NOTIFICATIONS = "notifications";

    private static final String TAG = "SendTokenTaskService";
    private static Registration registration = null;


    public static void runSendTokenTask(Context context) {
        GcmNetworkManager.getInstance(context).schedule(createTask());
    }

    private static OneoffTask createTask() {
        Log.d(Config.TAG, "SendTokenTaskService.createTask()");
        return new OneoffTask.Builder()
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setService(SendTokenTaskService.class)
                .setExecutionWindow(0, 60)
                .setPersisted(true)
                .setTag(TAG)
                .setUpdateCurrent(true)
                .build();
    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        int result = GcmNetworkManager.RESULT_SUCCESS;

        try {
            // Get updated InstanceID token.
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.i(Config.TAG, "FCM Registration Token: " + token);

            // subscribe to notifications
            FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_NOTIFICATIONS);

            //TODO: send test FCM to make sure the device can receive our messages
            sendRegistrationToServer(token);

            FcmHelper.saveFcmToken(this, token);

        } catch (Exception e) {

            Log.d(Config.TAG, "Failed to complete FCM token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Preferences.setFcmSentTokenToServer(this, false);
            result = GcmNetworkManager.RESULT_RESCHEDULE;

        }

        // Notify UI that registration has completed.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(REGISTRATION_COMPLETE));

        return result;
    }


    /**
     * Persist registration to App Engine backend.
     *
     * @param token The new token.
     */
    @WorkerThread
    private void sendRegistrationToServer(String token) throws IOException {
        Log.d(Config.TAG, "Send FCM token to the backend server");
        //if (true) throw new IOException("Test");
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
        registration.registerTopicNotifications(token).execute();
    }


}
