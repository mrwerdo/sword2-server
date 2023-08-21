package org.swordapp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class StatementAPI extends SwordAPIEndpoint {
    private static Logger log = LoggerFactory.getLogger(CollectionAPI.class);

    private final StatementManager sm;

    public StatementAPI(final StatementManager sm, final SwordConfiguration config) {
        super(config);
        this.sm = sm;
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
            // there may be some content negotiation going on
            Map<String, String> accept = this.getAcceptHeaders(req);
            String uri = this.getFullUrl(req);

            Statement statement = this.sm.getStatement(uri, accept, auth, this.config);

            // set the content type
            resp.setHeader("Content-Type", statement.getContentType());

            // set the last modified header
            // like: Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            Date lastModified = statement.getLastModified() != null ? statement.getLastModified() : new Date();
            resp.setHeader("Last-Modified", sdf.format(lastModified));

            // to set the content-md5 header we need to write the output to
            // a string and checksum it
            StringWriter writer = new StringWriter();
            statement.writeTo(writer);

            // write the content-md5 header
            String md5 = ChecksumUtils.hash(writer.toString());
            resp.setHeader("Content-MD5", md5);

            resp.getWriter().append(writer.toString());
            resp.getWriter().flush();

        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordError se) {
            this.swordError(req, resp, se);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
