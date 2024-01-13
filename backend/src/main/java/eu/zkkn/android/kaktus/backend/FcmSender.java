package eu.zkkn.android.kaktus.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Firebase cloud messages sender
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

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

        //TODO: Migrate from legacy HTTP to HTTP v1 API
        // So the analytics labels could be added to messages in order to have data for reports
        // in Firebase console
        // https://firebase.google.com/docs/cloud-messaging/understand-delivery#adding_analytics_labels_to_messages
        Sender sender = new Sender(API_KEY);

        Message msg = new Message.Builder()
                .addData("type", "notification")
                .addData("message", Utils.cropText(message, 1000))
                .addData("uri", CheckServlet.KAKTUS_DOBIJECKA_URL)
                .priority(Message.Priority.HIGH)
                //.dryRun(true)
                .build();

        // Send message to topic for notifications
        String topicName = Utils.isProduction() ? "notifications" : "notifications-debug";
        String topicFullName = Constants.TOPIC_PREFIX + topicName;
        LOG.info("Send message to topic: " + topicFullName);
        Result topicResult = trySendMessage(sender, msg, topicFullName);
        if (topicResult == null || topicResult.getMessageId() == null) {
            LOG.severe("Error when sending message to " + topicFullName);
        }
        if (topicResult != null) LOG.info(topicResult.toString());

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
