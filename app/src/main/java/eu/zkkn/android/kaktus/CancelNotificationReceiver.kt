package eu.zkkn.android.kaktus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import androidx.core.app.NotificationManagerCompat


class CancelNotificationReceiver : BroadcastReceiver() {

    companion object {

        private const val ACTION_CANCEL_NOTIFICATION = BuildConfig.APPLICATION_ID + ".intent.action.CANCEL_NOTIFICATION"
        private const val EXTRA_NOTIFICATION_ID = BuildConfig.APPLICATION_ID + ".extra.ID"

        @JvmStatic
        fun getIntent(context: Context, @IntRange(from = 0) notificationId: Int): Intent {
            return Intent(context, CancelNotificationReceiver::class.java).apply {
                action = ACTION_CANCEL_NOTIFICATION
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CANCEL_NOTIFICATION) {
            val id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Int.MIN_VALUE)
            if (id != Int.MIN_VALUE) NotificationManagerCompat.from(context).cancel(id)
        }
    }
}
