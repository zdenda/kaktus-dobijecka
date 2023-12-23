package eu.zkkn.android.kaktus.backend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class EndpointsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(EndpointsServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        switch (path) {
            case "/registration/v1/registerTopicNotifications":
                // Only used by old clients. Return status 204 to prevent errors
                LOG.info("Deprecated endpoint: " + path);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                break;
            case "/registration/v1/registerDevice":
                // Only used by super old clients, which shouldn't exist in the wild anymore
                LOG.warning("Removed endpoint: " + path);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

}
