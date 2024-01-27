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

        FcmSender.sendFcmToAll(message, debug == "true")
        resp.writer.println("OK")
    }
}
