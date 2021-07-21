package org.swordapp.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

    public void writeTo(final Writer out, final SwordConfiguration config) throws SwordServerException {
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    
            // Root element <sword:error>
            Document doc = docBuilder.newDocument();
            Element swordError = doc.createElementNS(UriRegistry.SWORD_TERMS_NAMESPACE, "sword:error");
            // Make the atom namespace the default (without prefix)
            swordError.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", UriRegistry.ATOM_NAMESPACE);
            swordError.setAttribute("href", this.errorUri);
            doc.appendChild(swordError);
    
            // Write Atom related parts
            // <sword:error><title>
            Element title = doc.createElementNS(UriRegistry.ATOM_NAMESPACE, "title");
            title.setTextContent("ERROR");
            swordError.appendChild(title);
    
            // <sword:error><updated>
            Element updated = doc.createElementNS(UriRegistry.ATOM_NAMESPACE, "updated");
            updated.setTextContent(DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
            swordError.appendChild(updated);
    
            // <sword:error><generator>
            Element generator = doc.createElementNS(UriRegistry.ATOM_NAMESPACE, "generator");
            generator.setAttribute("uri", config.generator());
            generator.setAttribute("version", config.generatorVersion());
            if (config.administratorEmail() != null) {
                generator.setTextContent(config.administratorEmail());
            }
            swordError.appendChild(generator);
    
            // <sword:error><summary>
            if (this.summary != null) {
                Element summary = doc.createElementNS(UriRegistry.ATOM_NAMESPACE, "summary");
                summary.setTextContent(this.summary);
                swordError.appendChild(summary);
            }
            
            // <sword:error><link rel="alternate">
            String alternate = config.getAlternateUrl();
            String altContentType = config.getAlternateUrlContentType();
            if (alternate != null && !"".equals(alternate)) {
                Element altLink = doc.createElementNS(UriRegistry.ATOM_NAMESPACE, "link");
                altLink.setAttribute("rel", "alternate");
                if (altContentType != null && !"".equals(altContentType)) {
                    altLink.setAttribute("type", altContentType);
                }
                altLink.setAttribute("href", alternate);
                swordError.appendChild(altLink);
            }
            
            // Write SWORD specific parts
            // <sword:error><sword:treatment>
            Element treatment = doc.createElementNS(UriRegistry.SWORD_TERMS_NAMESPACE, "sword:treatment");
            treatment.setTextContent("Processing failed");
            swordError.appendChild(treatment);
            
            // <sword:error><sword:verboseDescription>
            if (this.verboseDescription != null) {
                Element verbose = doc.createElementNS(UriRegistry.SWORD_TERMS_NAMESPACE, "sword:verboseDescription");
                verbose.setTextContent(this.verboseDescription);
                swordError.appendChild(verbose);
            }
            
            // Actually write the model to a stream
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, new StreamResult(out));
        } catch (TransformerException | ParserConfigurationException e) {
            throw new SwordServerException(e);
        }
    }
}
