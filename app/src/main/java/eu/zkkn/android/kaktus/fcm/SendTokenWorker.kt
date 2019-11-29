package eu.zkkn.android.kaktus.fcm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import eu.zkkn.android.kaktus.BuildConfig
import eu.zkkn.android.kaktus.Config
import eu.zkkn.android.kaktus.Helper
import eu.zkkn.android.kaktus.Preferences
import eu.zkkn.android.kaktus.backend.registration.Registration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class SendTokenWorker(
        private val appContext: Context,
        params: WorkerParameters
) : CoroutineWorker(appContext, params) {


    companion object {

        const val REGISTRATION_COMPLETE = "registrationComplete"

        @JvmStatic
        fun runSendTokenTask(context: Context) {
            val sendTokenTask = OneTimeWorkRequest.Builder(SendTokenWorker::class.java)
                    .setConstraints(
                            Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                    )
                    .build()
            Log.d(Config.TAG, "SendTokenWorker.runSendTokenTask()")
            WorkManager.getInstance(context).enqueue(sendTokenTask)
        }

    }


    private val registration: Registration by lazy {
        Registration.Builder(NetHttpTransport(), AndroidJsonFactory(), null)
                .setRootUrl(Config.BACKEND_ROOT_URL + "_ah/api/")
                .setGoogleClientRequestInitializer {
                    it.requestHeaders.userAgent = Helper.getUserAgent()
                    it.disableGZipContent = true
                }
                .build()
    }


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var result = Result.success()

        try {
            // Get updated InstanceID token.
            val token = FirebaseInstanceId.getInstance().token
            Log.i(Config.TAG, "FCM Registration Token: $token")

            val firebaseMessaging = FirebaseMessaging.getInstance()

            // subscribe to notifications
            firebaseMessaging.subscribeToTopic(FcmHelper.FCM_TOPIC_NOTIFICATIONS)

            // and also to debug notifications if this is a debug build
            if (BuildConfig.DEBUG) {
                firebaseMessaging.subscribeToTopic("${FcmHelper.FCM_TOPIC_NOTIFICATIONS}-debug")
            }

            if (token == null) return@withContext Result.retry()
            sendRegistrationToServer(token)

            FcmHelper.saveFcmToken(appContext, token)

            //TODO: send test FCM to make sure the device can receive our messages

        } catch (e: Exception) {

            Log.d(Config.TAG, "Failed to complete FCM token refresh", e)
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Preferences.setFcmSentTokenToServer(appContext, false)
            result = Result.retry()

        }

        // Notify UI that registration has completed.
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(Intent(REGISTRATION_COMPLETE))

        return@withContext result
    }


    /**
     * Persist registration to App Engine backend.
     *
     * @param token The new token.
     */
    @WorkerThread
    @Throws(IOException::class)
    private fun sendRegistrationToServer(token: String) {
        registration.registerTopicNotifications(token).execute()
    }

}
