package org.swordapp.server;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ErrorDocument {
    private String errorUri = null;
    private static final Map<String, Integer> ERROR_CODES = new HashMap<>();
    private String summary = null;
    private String verboseDescription = null;
    private int status;
    
    static {
        // set up the error codes mapping
        ERROR_CODES.put(UriRegistry.ERROR_BAD_REQUEST, HttpServletResponse.SC_BAD_REQUEST); // bad request
        ERROR_CODES.put(UriRegistry.ERROR_CHECKSUM_MISMATCH, HttpServletResponse.SC_PRECONDITION_FAILED); // precondition failed
        ERROR_CODES.put(UriRegistry.ERROR_CONTENT, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE); // unsupported media type
        ERROR_CODES.put(UriRegistry.ERROR_MEDIATION_NOT_ALLOWED, HttpServletResponse.SC_PRECONDITION_FAILED); // precondition failed
        ERROR_CODES.put(UriRegistry.ERROR_METHOD_NOT_ALLOWED, HttpServletResponse.SC_METHOD_NOT_ALLOWED); // method not allowed
        ERROR_CODES.put(UriRegistry.ERROR_TARGET_OWNER_UNKNOWN, HttpServletResponse.SC_FORBIDDEN); // forbidden
        ERROR_CODES.put(UriRegistry.ERROR_MAX_UPLOAD_SIZE_EXCEEDED, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE); // forbidden
    }

    public ErrorDocument(final String errorUri) {
        this(errorUri, -1, null, null);
    }

    public ErrorDocument(final String errorUri, final int status) {
        this(errorUri, status, null, null);
    }

    public ErrorDocument(final String errorUri, final String verboseDescription) {
        this(errorUri, -1, null, verboseDescription);
    }

    public ErrorDocument(final String errorUri, final int status, final String verboseDescription) {
        this(errorUri, status, null, verboseDescription);
    }

    public ErrorDocument(final String errorUri, final int status, final String summary, final String verboseDescription) {
        this.errorUri = errorUri;
        this.summary = summary;
        this.verboseDescription = verboseDescription;
        this.status = status;
    }

    public int getStatus() {
        if (this.status > -1) {
            return this.status;
        }

        if (errorUri != null && ERROR_CODES.containsKey(errorUri)) {
            return ERROR_CODES.get(errorUri);
        } else {
            return HttpServletResponse.SC_BAD_REQUEST; // bad request
        }
    }

    public void writeTo(final Writer out, final SwordConfiguration config) throws IOException, SwordServerException {
        // do the XML serialisation
        Element error = new Element("sword:error", UriRegistry.SWORD_TERMS_NAMESPACE);
        error.addAttribute(new Attribute("href", this.errorUri));

        // write some boiler-plate text into the document
        Element title = new Element("atom:title", UriRegistry.ATOM_NAMESPACE);
        title.appendChild("ERROR");
        Element updates = new Element("atom:updated", UriRegistry.ATOM_NAMESPACE);
        updates.appendChild(DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        Element generator = new Element("atom:generator", UriRegistry.ATOM_NAMESPACE);
        generator.addAttribute(new Attribute("uri", config.generator()));
        generator.addAttribute(new Attribute("version", config.generatorVersion()));
        if (config.administratorEmail() != null) {
            generator.appendChild(config.administratorEmail());
        }
        Element treatment = new Element("sword:treatment", UriRegistry.SWORD_TERMS_NAMESPACE);
        treatment.appendChild("Processing failed");

        error.appendChild(title);
        error.appendChild(updates);
        error.appendChild(generator);
        error.appendChild(treatment);

        // now add the operational bits
        if (this.summary != null) {
            Element summary = new Element("atom:summary", UriRegistry.ATOM_NAMESPACE);
            summary.appendChild(this.summary);
            error.appendChild(summary);
        }

        if (this.verboseDescription != null) {
            Element vd = new Element("sword:verboseDescription", UriRegistry.SWORD_TERMS_NAMESPACE);
            vd.appendChild(this.verboseDescription);
            error.appendChild(vd);
        }

        String alternate = config.getAlternateUrl();
        String altContentType = config.getAlternateUrlContentType();
        if (alternate != null && !"".equals(alternate)) {
            Element altLink = new Element("atom:link", UriRegistry.ATOM_NAMESPACE);
            altLink.addAttribute(new Attribute("rel", "alternate"));
            if (altContentType != null && !"".equals(altContentType)) {
                altLink.addAttribute(new Attribute("type", altContentType));
            }
            altLink.addAttribute(new Attribute("href", alternate));
            error.appendChild(altLink);
        }

        try {
            // now get it written out
            Document doc = new Document(error);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(baos, "ISO-8859-1");
            serializer.write(doc);
            out.write(baos.toString());
        } catch (UnsupportedEncodingException e) {
            throw new SwordServerException(e);
        }
    }
}
