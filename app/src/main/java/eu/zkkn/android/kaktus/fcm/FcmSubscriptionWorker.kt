package eu.zkkn.android.kaktus.fcm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import eu.zkkn.android.kaktus.BuildConfig
import eu.zkkn.android.kaktus.Config
import eu.zkkn.android.kaktus.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class FcmSubscriptionWorker(
        private val appContext: Context,
        params: WorkerParameters
) : CoroutineWorker(appContext, params) {


    companion object {

        const val WORKER_HAS_FINISHED = "WorkerHasFinished"
        const val PERIODIC_WORK_VERSION = 3

        private const val PERIODIC_WORK_NAME = "eu.zkkn.android.kaktus.work.PERIODIC_REFRESH"

        @JvmStatic
        fun runSubscribeToTopics(context: Context) {
            val sendTokenTask = OneTimeWorkRequest.Builder(FcmSubscriptionWorker::class.java)
                    .setConstraints(
                            Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                    )
                    .build()
            Log.d(Config.TAG, "FcmSubscriptionWorker.runSubscribeToTopics()")
            WorkManager.getInstance(context).enqueue(sendTokenTask)
        }

        @JvmStatic
        fun schedulePeriodicSubscriptionRefresh(context: Context) {
            if (Preferences.isPeriodicSubscriptionRefreshEnabled(context)) return

            Log.d(Config.TAG, "Schedule periodic refresh for FCM topic subscriptions")
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    PeriodicWorkRequest.Builder(FcmSubscriptionWorker::class.java, 28, TimeUnit.DAYS, 4, TimeUnit.DAYS)
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

            Preferences.setFcmToken(applicationContext, token)
            Preferences.setSubscribedToNotifications(applicationContext, true)
            Preferences.setLastSubscriptionRefresh(applicationContext)

            //TODO: send test FCM to make sure the device can receive our messages

        } catch (e: Exception) {

            Log.d(Config.TAG, "Failed attempt to subscribe for Topic notifications", e)
            FirebaseCrashlytics.getInstance().recordException(e)

            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            result = Result.retry()

        }

        // Notify UI that registration has completed.
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(Intent(WORKER_HAS_FINISHED))

        return@withContext result
    }

}
