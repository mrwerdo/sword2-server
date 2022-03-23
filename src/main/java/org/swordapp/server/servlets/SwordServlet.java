package org.swordapp.server.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swordapp.server.SwordConfiguration;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import java.lang.reflect.Constructor;

public class SwordServlet extends HttpServlet {
    private static Logger log = LoggerFactory.getLogger(SwordServlet.class);

    protected SwordConfiguration config;

    public void init() throws ServletException {
        // load the configuration implementation
        this.config = (SwordConfiguration) this.loadImplClass("config-impl", false);
    }

    protected Object loadImplClass(final String paramName, final boolean allowNull) throws ServletException {
        String className = getServletContext().getInitParameter(paramName);
        if (className == null) {
            if (allowNull) {
                return null;
            } else {
                log.error("'" + paramName + "' init parameter not set in Servlet context");
                throw new ServletException("'" + paramName + "' init parameter not set in Servlet context");
            }
        } else {
            try {
                // get public, zero args constructor of implementing class
                Constructor<?>[] constructors = Class.forName(className).getConstructors();
                Constructor<?> zeroConstructor = null;
                for (Constructor<?> i : constructors) {
                    if (i.getParameterCount() == 0) {
                        zeroConstructor = i;
                        break;
                    }
                }
                if (zeroConstructor == null) {
                    throw new IllegalArgumentException("Cannot find a public zero args constructor.");
                }
                
                Object obj = zeroConstructor.newInstance();
                log.info("Using " + className + " as '" + paramName + "'");
                return obj;
            } catch (Exception e) {
                log.error("Unable to instantiate class from '" + paramName + "': " + className);
                throw new ServletException("Unable to instantiate class from '" + paramName + "': " + className, e);
            }
        }
    }
}
