package eu.zkkn.android.kaktus

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DebugInfoDialog(val context: Context) {

    fun show() {
        val builder = AlertDialog.Builder(context)

        val title =  context.getString(R.string.dialog_debug_info_title)
        val progressBar = ProgressBar(context)

        builder.setTitle(title)
        builder.setView(progressBar)
        builder.setPositiveButton(R.string.dialog_debug_info_button_ok) { dialog, _ ->
            dialog.dismiss()
        }
        // message and neutral button must be set in builder, so they could be used later
        builder.setMessage("")
        builder.setNeutralButton(R.string.dialog_debug_info_copy_to_clipboard) { _, _ ->
            Toast.makeText(context, R.string.dialog_debug_info_copy_unavailable, Toast.LENGTH_SHORT).show()
        }

        val subscribed = Preferences.isSubscribedToNotifications(context)
        val lastRefresh = Preferences.getLastSubscriptionRefreshTime(context)
        var firebaseId: String

        val dialog: AlertDialog = builder.show().apply {
            // disable copy button since it shouldn't be used until all info is loaded
            getButton(DialogInterface.BUTTON_NEUTRAL).isEnabled = false
        }

        dialog.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                firebaseId = FirebaseInstallations.getInstance().id.await()
            }

            progressBar.visibility = View.GONE

            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).apply {
                setOnClickListener { _ ->
                    val json = JSONObject()
                    json.put("instanceId", firebaseId)
                    json.put("lastRefresh",
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                            .format(Date(lastRefresh)))
                    json.put("subscribed", subscribed)
                    Helper.copyToClipboard(context, title, json.toString())
                }
                isEnabled = true
            }

            dialog.setMessage(Helper.formatHtml(
                "<br/>%1\$s<br/><br/>%2\$s<br/><br/>%3\$s<br/><br/><br/><small>* %4\$s</small>",
                context.getString(R.string.dialog_debug_info_firebase_instance_id, firebaseId),
                context.getString(R.string.dialog_debug_info_refresh_time, lastRefresh),
                context.getString(R.string.dialog_debug_info_topic_subscription, subscribed),
                context.getString(R.string.dialog_debug_info_warning)))

        }

    }

}
