package eu.zkkn.android.kaktus.backend;

import com.googlecode.objectify.ObjectifyService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * OfyHelper, a ServletContextListener, is setup in web.xml to run after the application starts up
 * and before it services the first request.
 */
public class OfyHelper implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // This will be invoked as part of a warmup request,
        // or the first user request if no warmup request.

        // For Objectify v6: ObjectifyService.init();
        ObjectifyService.register(RegistrationRecord.class);
        ObjectifyService.register(ParseResult.class);
        ObjectifyService.register(EmailLog.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // App Engine does not currently invoke this method.
    }

}
