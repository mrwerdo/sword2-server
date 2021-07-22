package org.swordapp.server.servlets;

import org.swordapp.server.ServiceDocumentAPI;
import org.swordapp.server.ServiceDocumentManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceDocumentServletDefault extends SwordServlet {
    private static final long serialVersionUID = -5608750960628797123L;
    protected transient ServiceDocumentAPI api;

    public void init() throws ServletException {
        super.init();

        // load the service document implementation
        ServiceDocumentManager sdm = (ServiceDocumentManager) this.loadImplClass("service-document-impl", false);

        // load the api
        this.api = new ServiceDocumentAPI(sdm, this.config);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        this.api.get(req, resp);
    }
}
