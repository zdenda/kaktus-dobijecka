package eu.zkkn.android.kaktus.fcm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import eu.zkkn.android.kaktus.BuildConfig
import eu.zkkn.android.kaktus.Config
import eu.zkkn.android.kaktus.Helper
import eu.zkkn.android.kaktus.Preferences
import eu.zkkn.android.kaktus.backend.registration.Registration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit


class SendTokenWorker(
        private val appContext: Context,
        params: WorkerParameters
) : CoroutineWorker(appContext, params) {


    companion object {

        const val REGISTRATION_COMPLETE = "registrationComplete"
        const val PERIODIC_WORK_VERSION = 2

        private const val PERIODIC_WORK_NAME = "eu.zkkn.android.kaktus.work.PERIODIC_REFRESH"

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

        @JvmStatic
        fun schedulePeriodicRefresh(context: Context) {
            if (Preferences.isPeriodicSubscriptionRefreshEnabled(context)) return

            Log.d(Config.TAG, "Schedule periodic refresh for FCM topic subscriptions")
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    //Interval resets (job is rescheduled) on device reboot, so it might never run if interval is too long
                    PeriodicWorkRequest.Builder(SendTokenWorker::class.java, 30, TimeUnit.DAYS, 4, TimeUnit.DAYS)
                            .addTag(PERIODIC_WORK_NAME)
                            .setBackoffCriteria(
                                    BackoffPolicy.EXPONENTIAL,
                                    WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS * 4,
                                    TimeUnit.MILLISECONDS
                            )
                            .setConstraints(
                                    Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .setRequiresCharging(true)
                                            .build()
                            )
                            .build()
            )
            Preferences.setPeriodicSubscriptionRefresh(context)
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
            val token: String = Firebase.messaging.token.await()
            Log.i(Config.TAG, "FCM Registration Token: $token")

            val firebaseMessaging = FirebaseMessaging.getInstance()

            // subscribe to notifications
            firebaseMessaging.subscribeToTopic(FcmHelper.FCM_TOPIC_NOTIFICATIONS)

            // and also to debug notifications if this is a debug build
            if (BuildConfig.DEBUG) {
                firebaseMessaging.subscribeToTopic("${FcmHelper.FCM_TOPIC_NOTIFICATIONS}-debug")
            }

            // probably false positive https://youtrack.jetbrains.com/issue/KT-39684
            @Suppress("BlockingMethodInNonBlockingContext")
            sendRegistrationToServer(token)

            FcmHelper.saveFcmToken(appContext, token)

            Preferences.setLastSubscriptionRefresh(applicationContext)

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
