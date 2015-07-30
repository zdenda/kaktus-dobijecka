package eu.zkkn.android.kaktus;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class IdListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token
        Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
    }

}
