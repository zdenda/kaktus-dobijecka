package eu.zkkn.android.kaktus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.core.app.NotificationManagerCompat
import eu.zkkn.android.kaktus.fcm.MyFcmListenerService
import java.util.Date


class PostponeNotificationReceiver : BroadcastReceiver() {

    companion object {

        private const val ACTION_POSTPONE_NOTIFICATION = BuildConfig.APPLICATION_ID + ".intent.action.POSTPONE_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = BuildConfig.APPLICATION_ID + ".extra.ID"
        private const val EXTRA_NOTIFICATION_MESSAGE = BuildConfig.APPLICATION_ID + ".extra.MESSAGE"
        private const val EXTRA_NOTIFICATION_URI = BuildConfig.APPLICATION_ID + ".extra.URI"
        private const val EXTRA_NOTIFICATION_START = BuildConfig.APPLICATION_ID + ".extra.START"
        private const val EXTRA_NOTIFICATION_END = BuildConfig.APPLICATION_ID + ".extra.END"

        @JvmStatic
        fun getIntent(
            context: Context,
            @IntRange(from = 0) notificationId: Int,
            message: String,
            uri: String?,
            start: String,
            end: String
        ): Intent {
            return Intent(context, PostponeNotificationReceiver::class.java).apply {
                action = ACTION_POSTPONE_NOTIFICATION
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
                putExtra(EXTRA_NOTIFICATION_URI, uri)
                putExtra(EXTRA_NOTIFICATION_START, start)
                putExtra(EXTRA_NOTIFICATION_END, end)
            }
        }

    }


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_POSTPONE_NOTIFICATION) {
            val id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Int.MIN_VALUE)
            val message = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE)
            val uri = intent.getStringExtra(EXTRA_NOTIFICATION_URI)
            val start = intent.getStringExtra(EXTRA_NOTIFICATION_START)
            //val end = intent.getStringExtra(EXTRA_NOTIFICATION_END)

            if (id == Int.MIN_VALUE || message.isNullOrEmpty() || start.isNullOrEmpty()) return

            val startDate = start.toDateOrNull("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
            if (startDate == null) return

            // Check if the start date has already passed
            val now = Date()
            if (now.after(startDate)) {
                MyFcmListenerService.showNotificationWithoutPostpone(context, message, uri)
                Toast.makeText(context, context.getString(R.string.error_cannot_postpone),
                    Toast.LENGTH_LONG).show()
                return
            }

            NotificationManagerCompat.from(context).cancel(id)
            PostponeNotificationWorker.schedulePostponedNotification(context, startDate, message, uri)
        }
    }

}
