package eu.zkkn.android.kaktus.backend

import java.lang.RuntimeException
import javax.servlet.ServletContext


object ServletContextHolder {

    private var servletContext: ServletContext? = null

    fun setServletContext(servletContext: ServletContext) {
        this.servletContext = servletContext
    }

    fun getServletContext(): ServletContext {
        if (servletContext == null) throw RuntimeException("ServletContext has not been set.")
        return this.servletContext!!
    }

}
