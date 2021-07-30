package org.swordapp.server;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.net.URLDecoder;
import java.util.Date;

public class CollectionAPI extends SwordAPIEndpoint {
    private static Logger log = LoggerFactory.getLogger(CollectionAPI.class);

    protected final CollectionListManager clm;
    protected final CollectionDepositManager cdm;

    public CollectionAPI(final CollectionListManager clm, final CollectionDepositManager cdm, final SwordConfiguration config) {
        super(config);
        this.clm = clm;
        this.cdm = cdm;
    }

    public void get(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.get(req, resp);

        // first find out if this is supported
        if (this.clm == null) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

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
            Feed feed = this.clm.listCollectionContents(new IRI(this.getFullUrl(req)), auth, this.config);

            // since the spec doesn't require the collection to be listable, this might
            // give us back null
            if (feed == null) {
                // method not allowed
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "This server does not support listing collection contents");
                return;
            }

            // otherwise process and return
            this.addGenerator(feed, this.config);

            resp.setHeader("Content-Type", "application/atom+xml;type=feed");
            feed.writeTo(resp.getWriter());
            resp.getWriter().flush();
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (SwordError se) {
            this.swordError(req, resp, se);
        }
    }

    public void post(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // let the superclass prepare the request/response objects
        super.post(req, resp);

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

        Deposit deposit = null;
        try {
            // the first thing to do is determine what the deposit type is:
            String contentType = this.getContentType(req);
            boolean isMultipart = contentType.startsWith("multipart/related");
            boolean isEntryOnly = contentType.startsWith("application/atom+xml");
            boolean isBinaryOnly = !isMultipart && !isEntryOnly;

            // get the common HTTP headers before leaping into the deposit type specific processes

            // According to http://www.ietf.org/rfc/rfc5023.txt Section 9.7.1 The Slug must be the percent encoded
            // UTF-8 byte sequence of the text to use as the value.
            // More about this header: https://blog.cdivilly.com/2016/03/01/slug-http-header/
            // As it's optional, make a nullsafe decode.
            String slug = null;
            if (req.getHeader("Slug") != null) {
                slug = URLDecoder.decode(req.getHeader("Slug"), StandardCharsets.UTF_8);
            }
            boolean inProgress = this.getInProgress(req);

            deposit = new Deposit();
            deposit.setInProgress(inProgress);
            deposit.setSlug(slug);
            DepositReceipt receipt = null;

            // do the different kinds of deposit details extraction
            if (isMultipart) {
                this.addDepositPropertiesFromMultipart(deposit, req);
            } else if (isEntryOnly) {
                this.addDepositPropertiesFromEntry(deposit, req);
            } else if (isBinaryOnly) {
                this.addDepositPropertiesFromBinary(deposit, req);
            }

            // now send the deposit to the implementation for processing
            String colUri = this.getFullUrl(req);
            receipt = this.cdm.createNew(colUri, deposit, auth, this.config);
            this.addGenerator(receipt, this.config);

            // prepare and return the response
            IRI location = receipt.getLocation();
            if (location == null) {
                throw new SwordServerException("No Location found in Deposit Receipt; unable to send valid response");
            }

            resp.setStatus(HttpServletResponse.SC_CREATED); // Created
            if (this.config.returnDepositReceipt() && !receipt.isEmpty()) {
                resp.setHeader("Content-Type", "application/atom+xml;type=entry");
                resp.setHeader("Location", location.toString());

                // set the last modified header
                // like: Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
                Date lastModified = receipt.getLastModified() != null ? receipt.getLastModified() : new Date();
                resp.setHeader("Last-Modified", sdf.format(lastModified));

                // to set the content-md5 header we need to write the output to
                // a string and checksum it
                StringWriter writer = new StringWriter();
                Entry responseEntry = receipt.getAbderaEntry();
                responseEntry.writeTo(writer);

                // write the content-md5 header
                String md5 = ChecksumUtils.hash(writer.toString());
                resp.setHeader("Content-MD5", md5);

                resp.getWriter().append(writer.toString());
                resp.getWriter().flush();
            } else {
                resp.setHeader("Location", location.toString());
            }
        } catch (SwordError se) {
            // get rid of any temp files used
            this.cleanup(deposit);

            this.swordError(req, resp, se);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        } catch (SwordAuthException e) {
            // get rid of any temp files used
            this.cleanup(deposit);

            // authentication actually failed at the server end; not a SwordError, but
            // need to throw a 403 Forbidden
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException is thrown in case .post() receives a "slug" with illegally encoded characters
            throw new ServletException(e);
        } finally {
            // get rid of any temp files used
            this.cleanup(deposit);
        }
    }

    protected void addGenerator(final DepositReceipt doc, final SwordConfiguration config) {
        Element generator = this.getGenerator(this.config);
        if (generator != null) {
            doc.getWrappedEntry().addExtension(generator);
        }
    }

    protected void addGenerator(final Feed doc, final SwordConfiguration config) {
        Element generator = this.getGenerator(this.config);
        if (generator != null) {
            doc.addExtension(generator);
        }
    }

}
