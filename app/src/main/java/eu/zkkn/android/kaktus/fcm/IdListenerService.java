package eu.zkkn.android.kaktus.fcm;

import com.google.firebase.iid.FirebaseInstanceIdService;


public class IdListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        SendTokenTaskService.runSendTokenTask(this);
    }

}
