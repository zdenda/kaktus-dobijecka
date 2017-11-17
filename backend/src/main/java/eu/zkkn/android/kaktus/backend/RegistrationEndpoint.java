/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package eu.zkkn.android.kaktus.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.logging.Logger;

import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 * <p/>
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * <p/>
 * TODO: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(name = "registration", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.kaktus.android.zkkn.eu", ownerName = "backend.kaktus.android.zkkn.eu", packagePath = ""))
public class RegistrationEndpoint {

    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Register a device to the backend
     *
     * @param regId The Google Cloud Messaging registration Id to add
     */
    // force path, otherwise it would be "registerDevice/{regId}", which causes problem for Jetty (App Engine on localhost).
    // regId might contain colon (":") and jetty returns error 404 if colon is in URL path
    @Deprecated
    @ApiMethod(name = "register", path = "registerDevice")
    public void registerDevice(@Named("regId") String regId) {
        if (findRecord(regId) != null) {
            log.info("Device " + regId + " already registered, skipping register");
            return;
        }
        RegistrationRecord record = new RegistrationRecord();
        record.setRegId(regId);
        ofy().save().entity(record).now();
    }

    /**
     * Register a device and set flag for notifications topic
     *
     * @param token The Firebase Cloud Messaging token (Registration ID in old GCM terminology)
     */
    // force path, otherwise it would be "registerTopicNotifications/{token}", which causes problem for Jetty (App Engine on localhost).
    // token might contain colon (":") and jetty returns error 404 if colon is in URL path
    @ApiMethod(name = "registerTopicNotifications", path = "registerTopicNotifications")
    public void registerTopicNotifications(@Named("token") String token) {
        RegistrationRecord record = findRecord(token);
        if (record == null) {
            record = new RegistrationRecord();
            record.setRegId(token);
        }
        record.setTopicNotifications(true);
        ofy().save().entity(record).now();
    }

    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
    }

}
