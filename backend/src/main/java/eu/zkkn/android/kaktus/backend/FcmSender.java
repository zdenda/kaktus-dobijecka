package eu.zkkn.android.kaktus.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Firebase cloud messages sender
 *
 * If used as a Push Queue Task the limit for execution is 10 min, otherwise it's 1 min
 */
public class FcmSender extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FcmSender.class.getName());

    /**
     * Api Keys can be obtained from the google cloud console
     */
    private static final String API_KEY = System.getProperty("gcm.api.key");

    private static final String PARAM_MESSAGE_NAME = "msg";


    public static void sendFcmToAll(String message) {
        QueueFactory.getDefaultQueue().add(
                TaskOptions.Builder.withUrl("/tasks/fcm-sender")
                        .param(PARAM_MESSAGE_NAME, message)
                        .retryOptions(RetryOptions.Builder.withTaskRetryLimit(3)));
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        LOG.info("Start sending FCMs");

        @Nullable
        String message = req.getParameter(PARAM_MESSAGE_NAME);

        if (message != null && message.trim().length() != 0) {
            sendMessage(message);
        } else {
            LOG.warning("The message to send it is empty.");
        }

        LOG.info("Finish Sending FCMs");

    }


    /**
     * Send FCM to all registered devices
     *
     * @param message The message to send
     */
    private void sendMessage(@Nonnull String message) {

        Sender sender = new Sender(API_KEY);

        Message msg = new Message.Builder()
                .addData("type", "notification")
                .addData("message", Utils.cropText(message, 1000))
                .addData("uri", CheckServlet.KAKTUS_WEB_URL)
                .build();

        //TODO: use FCM topic instead of sending the message to each device separately
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).list();

        int successCounter = 0;
        int errorCounter = 0;

        List<RegistrationRecord> updateEntities = new ArrayList<>();
        List<Long> deleteIds = new ArrayList<>();

        for (RegistrationRecord record : records) {

            Result result = trySendMessage(sender, msg, record.getRegId());

            if (result != null && result.getMessageId() != null) {
                LOG.info("Message sent to " + record.getRegId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update it in the datastore
                    LOG.info(String.format("Registration ID changed for %s updating to %s",
                            record.getRegId(), canonicalRegId));
                    record.setRegId(canonicalRegId);
                    updateEntities.add(record);
                }
                successCounter++;
            } else {
                errorCounter++;
                if (result == null) continue;
                String error = result.getErrorCodeName();
                if (Constants.ERROR_NOT_REGISTERED.equals(error)) {
                    // if the device is no longer registered with Gcm, remove it from the datastore
                    LOG.warning(String.format("Registration ID %s no longer registered with GCM, removing from datastore",
                            record.getRegId()));
                    deleteIds.add(record.id);
                } else {
                    LOG.warning(String.format("Error when sending message to Registration ID [%s]: %s",
                            record.getRegId(), error));
                }
            }

        }

        // do the postponed update/delete for changed/deleted entities
        LOG.info(String.format(Locale.US, "Update %d records.", updateEntities.size()));
        ofy().save().entities(updateEntities).now();
        // give the datastore some break
        Utils.sleep(5_000);
        LOG.info(String.format(Locale.US, "Delete %d records.", deleteIds.size()));
        ofy().delete().type(RegistrationRecord.class).ids(deleteIds).now();

        LOG.info(String.format(Locale.US, "Total devices: %d [success: %d, error: %d]",
                records.size(), successCounter, errorCounter));

    }

    @Nullable
    private Result trySendMessage(Sender sender, Message msg, String registrationId) {
        Result result = null;
        try {
            result = sender.send(msg, registrationId, 5);
        } catch (IOException e) {
            LOG.warning(String.format("Message couldn't be sent: %s %s",
                    e.getClass().getName(), e.getMessage()));
        }
        return result;
    }

}
