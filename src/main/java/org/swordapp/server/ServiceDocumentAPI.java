package org.swordapp.server;

import org.apache.abdera.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServiceDocumentAPI extends SwordAPIEndpoint {
    private static Logger log = LoggerFactory.getLogger(ServiceDocumentAPI.class);

    protected final ServiceDocumentManager sdm;

    public ServiceDocumentAPI(final ServiceDocumentManager sdm, final SwordConfiguration config) {
        super(config);
        this.sdm = sdm;
    }

    public void get(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.get(req, resp);

        // do the initial authentication
        AuthCredentials auth = null;
        try {
            auth = this.getAuthCredentials(req);
        } catch (SwordAuthException e) {
            if (e.isRetry()) {
                String s = "Basic realm=\"SWORD2\"";
                resp.setHeader("WWW-Authenticate", s);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
        }

        try {
            String sdUri = this.getFullUrl(req);

            // delegate to the implementation to get the service document itself
            ServiceDocument serviceDocument = this.sdm.getServiceDocument(sdUri, auth, this.config);
            this.addGenerator(serviceDocument, this.config);

            // set the content-type and write the service document to the output stream
            resp.setHeader("Content-Type", "application/atomserv+xml");
            serviceDocument.getAbderaService().writeTo(resp.getWriter());
        } catch (SwordError se) {
            // this is a SWORD level error, to be thrown to the client appropriately
            this.swordError(req, resp, se);
        } catch (SwordServerException e) {
            // this is something else, to be raised as an internal server error
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } finally {
            // flush the output stream
            resp.getWriter().flush();
        }
    }

    protected void addGenerator(final ServiceDocument doc, final SwordConfiguration config) {
        Element generator = this.getGenerator(this.config);
        if (generator != null) {
            doc.getWrappedService().addExtension(generator);
        }
    }
}
