package eu.zkkn.android.kaktus.backend

import com.googlecode.objectify.ObjectifyService
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

/**
 * This ServletContextListener is setup as WebListener to run after the application starts up
 * and before it services the first request.
 */
@WebListener
class Bootstrapper : ServletContextListener {

    override fun contextInitialized(sce: ServletContextEvent?) {
        // This will be invoked as part of a warmup request,
        // or the first user request if no warmup request.

        if (sce != null) {
            ServletContextHolder.setServletContext(sce.servletContext)
        }

        // For Objectify v6: ObjectifyService.init();
        //ObjectifyService.register(RegistrationRecord.class);
        ObjectifyService.register(ParseResult::class.java)
        ObjectifyService.register(EmailLog::class.java)
    }

    override fun contextDestroyed(sce: ServletContextEvent) {
        // App Engine does not currently invoke this method.
    }

}
