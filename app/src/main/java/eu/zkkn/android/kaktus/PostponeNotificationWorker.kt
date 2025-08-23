package eu.zkkn.android.kaktus

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService
import java.util.Date
import java.util.concurrent.TimeUnit


class PostponeNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val KEY_URI = "KEY_URI"

        fun schedulePostponedNotification(context: Context, start: Date, message: String, uri: String?) {
            val inputData = workDataOf(
                KEY_MESSAGE to message,
                KEY_URI to uri
            )

            val workRequest = OneTimeWorkRequestBuilder<PostponeNotificationWorker>()
                //FIXME: What will happen if postpone is clicked after start time, or even after end time
                .setInitialDelay(start.time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            Log.d(Config.TAG, "PostponeNotificationWorker.schedulePostponedNotification()")
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result {
        val message = inputData.getString(KEY_MESSAGE)
        val uri = inputData.getString(KEY_URI)

        if (message.isNullOrEmpty()) return Result.failure()

        MyFcmListenerService.showNotificationWithoutPostpone(applicationContext, message, uri)
        return Result.success()
    }

}
