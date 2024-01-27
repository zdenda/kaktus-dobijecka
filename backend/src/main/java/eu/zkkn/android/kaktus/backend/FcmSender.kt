package eu.zkkn.android.kaktus.backend

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.RetryOptions
import com.google.appengine.api.taskqueue.TaskOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FcmOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.util.logging.Logger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Firebase cloud messages sender
 * If used as a Push Queue Task the limit for execution is 10 min, otherwise it's 1 min
 */
class FcmSender : HttpServlet() {

    private val log = Logger.getLogger(this::class.java.name)

    private val firebaseMessaging: FirebaseMessaging by lazy {
        val googleCredentials = GoogleCredentials.fromStream(
            ServletContextHolder.getServletContext()
                .getResourceAsStream("/WEB-INF/serviceAccountKey.json")
        )
        val options = FirebaseOptions.builder().setCredentials(googleCredentials).build()
        FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options))
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        log.info("Start sending FCMs")
        val message = req.getParameter(PARAM_MESSAGE_NAME)
        if (message != null && message.trim().isNotEmpty()) {
            sendMessage(message)
        } else {
            log.warning("The message to send is empty.")
        }
        log.info("Finish Sending FCMs")
    }

    /**
     * Send FCM to all registered devices
     *
     * @param message The message to send
     */
    private fun sendMessage(message: String) {
        // Send message to topic for notifications
        val topicName = if (Utils.isProduction()) "notifications" else "notifications-debug"
        val fcmMessage = Message.builder()
            .setTopic(topicName)
            .putData("type", "notification")
            .putData("message", Utils.cropText(message, 1000))
            .putData("uri", CheckServlet.KAKTUS_DOBIJECKA_URL)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build()
            )
            .setFcmOptions(FcmOptions.withAnalyticsLabel(topicName))
            .build()


        log.info("Send message to topic: $topicName")
        val messageId = send(fcmMessage)
        if (messageId.isNullOrEmpty()) {
            log.severe("Error when sending message to $topicName")
        }
        if (messageId != null) log.info("Message ID: $messageId")
    }

    private fun send(message: Message): String? {
        // perform only a dry run if not in production
        val dryRun = !Utils.isProduction()
        if (dryRun) log.warning("FCM messages are sent only from Production environment")
        return firebaseMessaging.send(message, dryRun)
    }


    companion object {

        private const val PARAM_MESSAGE_NAME = "msg"

        @JvmStatic
        fun sendFcmToAll(message: String?) {
            QueueFactory.getDefaultQueue().add(
                TaskOptions.Builder.withUrl("/tasks/fcm-sender")
                    .param(PARAM_MESSAGE_NAME, message)
                    .retryOptions(RetryOptions.Builder.withTaskRetryLimit(3))
            )
        }

    }

}
