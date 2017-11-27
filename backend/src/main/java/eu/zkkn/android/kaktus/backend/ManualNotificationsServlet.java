package eu.zkkn.android.kaktus.backend;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ManualNotificationsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String message = req.getParameter("message");
        String fcmServerKey = req.getParameter("key");

        if (message == null || message.length() == 0
                || fcmServerKey == null || fcmServerKey.length() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required field");
            return;
        }

        if (!fcmServerKey.equals(System.getProperty("gcm.api.key"))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad FCM Server key");
            return;
        }

        FcmSender.sendFcmToAll(message);
        resp.getWriter().println("OK");
    }
}
