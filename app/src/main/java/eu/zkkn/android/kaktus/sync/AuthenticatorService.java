package eu.zkkn.android.kaktus.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * A bound Service that instantiates the authenticator
 * when started.
 */

public class AuthenticatorService extends Service {

    /** Instance field that stores the authenticator object */
    private Authenticator mAuthenticator;


    @Override
    public void onCreate() {
        super.onCreate();
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
