package eu.zkkn.android.kaktus.backend

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ManualNotificationsServlet : HttpServlet() {

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val message: String? = req.getParameter("message")
        val fcmServerKey: String? = req.getParameter("key")
        val debug: String? = req.getParameter("debug")

        if (message.isNullOrEmpty() || fcmServerKey.isNullOrEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required field")
            return
        }

        if (fcmServerKey != System.getProperty("gcm.api.key")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad FCM Server key")
            return
        }

        val isDebug = debug.toBoolean()

        FcmSender.sendFcmToAll(message, debug = isDebug)

        val adminEmail = System.getProperty("admin.email")
        if (!adminEmail.isNullOrEmpty()) {
            val subject = if (isDebug) "[DEBUG] Manual Notification Sent" else "Manual Notification Sent"
            Utils.sendEmail(
                "admin",
                adminEmail,
                subject,
                "Manual notification was sent with the following message:\n\n$message"
            )
        }

        resp.writer.println("OK")
    }
}
