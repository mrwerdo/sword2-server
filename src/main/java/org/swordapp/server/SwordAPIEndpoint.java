package org.swordapp.server;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ParameterParser;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SwordAPIEndpoint {
    protected final SwordConfiguration config;

    private static Logger log = LoggerFactory.getLogger(SwordAPIEndpoint.class);

    protected SwordAPIEndpoint(final SwordConfiguration config) {
        this.config = config;
    }

    public void get(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void post(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void put(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void delete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    protected AuthCredentials getAuthCredentials(final HttpServletRequest request) throws SwordAuthException {
        return this.getAuthCredentials(request, false);
    }

    protected AuthCredentials getAuthCredentials(final HttpServletRequest request, final boolean allowUnauthenticated) throws SwordAuthException {
        AuthCredentials auth = null;
        String authType = this.config.getAuthType(); // ideally Basic, but may be "none"
        String obo = "";
        log.info("Auth type = " + authType);

        // If we are insisting on "a" form of authentication that is not of type "none"
        if (!authType.equalsIgnoreCase("none")) {
            // Has the user passed authentication details
            String authHeader = request.getHeader("Authorization");

            // Is there an On-Behalf-Of header?
            obo = request.getHeader("On-Behalf-Of");

            // Which authentication scheme do we recognise (should only be Basic)
            boolean isBasic = authType.equalsIgnoreCase("basic");

            if (isBasic && (authHeader == null || authHeader.equals(""))) {
                if (allowUnauthenticated) {
                    log.debug("No Authentication Credentials supplied/required");
                    auth = new AuthCredentials(null, null, obo);
                    return auth;
                } else {
                    throw new SwordAuthException(true);
                }
            } else if (isBasic) {
                // decode the auth header and populate the authcredentials object for return
                String[] userPass = this.decodeAuthHeader(authHeader);
                auth = new AuthCredentials(userPass[0], userPass[1], obo);
            } else {
                throw new SwordAuthException("Server is not properly configured for authentication");
            }
        } else {
            log.debug("No Authentication Credentials supplied/required");
            auth = new AuthCredentials(null, null, obo);
        }

        return auth;
    }

    protected String[] decodeAuthHeader(final String encodedHeader) throws SwordAuthException {
        // we have an authentication header, so parse it
        String[] authBits = encodedHeader.split(" ");

        // Auth header doesn't have 2 parts (Basic, [base 64 username/password])?
        if (authBits.length != 2) {
            log.error("Malformed Authorization header");
            throw new SwordAuthException("Malformed Authorization header");
        }

        // is this basic auth? if not, we don't support it
        if (!"Basic".equalsIgnoreCase(authBits[0].trim())) {
            log.warn("Authentication method not supported: " + authBits[0]);
            throw new SwordAuthException("Authentication method not supported: " + authBits[0]);
        }

        // get the username and password out of the base64 encoded Basic auth string
        byte[] base64Creds = authBits[1].trim().getBytes(StandardCharsets.UTF_8);
        String unencodedCreds = new String(Base64.getDecoder().decode(base64Creds), StandardCharsets.UTF_8);
        String[] userPass = unencodedCreds.split(":", 2);

        // did we get a username and password?
        if (userPass.length != 2) {
            log.error("Malformed Authorization header; unable to determine username/password boundary");
            throw new SwordAuthException("Malformed Authorization header; unable to determine username/password boundary");
        }

        return userPass;
    }

    protected String getFullUrl(final HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        String q = req.getQueryString();
        if (q != null && !"".equals(q)) {
            url += "?" + q;
        }
        return url;
    }

    protected void storeAndCheckBinary(final Deposit deposit, final SwordConfiguration config) throws SwordServerException, SwordError {
        // we require an input stream for this to work
        if (deposit.getInputStream() == null) {
            throw new SwordServerException("Attempting to store and check deposit which has no input stream");
        }

        if (!config.storeAndCheckBinary()) {
            return;
        }

        String tempDirectory = config.getTempDirectory();
        if (tempDirectory == null) {
            throw new SwordServerException("Store and Check operation requested, but no tempDirectory specified in config");
        }

        String filename = tempDirectory + File.separator + "SWORD-" + UUID.randomUUID();
            
        try (
            InputStream inputStream = deposit.getInputStream();
            OutputStream outputStream = new FileOutputStream(filename);
        ) {
            // get the things we might want to compare
            String receivedMD5 = ChecksumUtils.hashAndCopy(inputStream, outputStream);
            log.debug("Received filechecksum: " + receivedMD5);
            String md5 = deposit.getMd5();
            log.debug("Received file checksum header: " + md5);

            if (md5 != null && !md5.equals(receivedMD5)) {
                log.debug("Bad MD5 for file. Aborting with appropriate error message");
                String msg = "The received MD5 checksum for the deposited file did not match the checksum sent by the deposit client";
                throw new SwordError(UriRegistry.ERROR_CHECKSUM_MISMATCH, msg);
            }
        } catch (IOException e) {
            throw new SwordServerException(e);
        }

        // Check the size is OK
        File file = new File(filename);
        // Set the file to be deposited
        deposit.setFile(file);

        long fLength = file.length(); // in bytes
        if (config.getMaxUploadSize() != -1 && fLength > config.getMaxUploadSize()) {
            String msg = "The uploaded file exceeded the maximum file size this server will accept (the file is " + fLength
                    + " bytes but the server will only accept files as large as " + config.getMaxUploadSize() + " bytes)";
            throw new SwordError(UriRegistry.ERROR_MAX_UPLOAD_SIZE_EXCEEDED, msg);
        }
    
        log.debug("Package temporarily stored as: " + filename);
    }

    protected void addDepositPropertiesFromMultipart(final Deposit deposit, final HttpServletRequest req) throws ServletException, IOException, SwordError {
        // Parse the request for files (using the fileupload commons library)
        List<FileItem> items = this.getPartsFromRequest(req);
        for (FileItem item : items) {
            // find out which part we are looking at
            String contentDisposition = item.getHeaders().getHeader("Content-Disposition");
            String name = this.getContentDispositionValue(contentDisposition, "name");

            if ("atom".equals(name)) {
                parseEntryFromInputStream(deposit, item.getInputStream());
            } else if ("payload".equals(name)) {
                String md5 = item.getHeaders().getHeader("Content-MD5");
                String packaging = item.getHeaders().getHeader("Packaging");
                String filename = this.getContentDispositionValue(contentDisposition, "filename");
                if (filename == null || "".equals(filename)) {
                    throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Filename could not be extracted from Content-Disposition");
                }
                String ct = item.getContentType();
                String mimeType = "application/octet-stream";
                if (ct != null) {
                    String[] bits = ct.split(";");
                    mimeType = bits[0].trim();
                }
                InputStream mediaPart = item.getInputStream();

                deposit.setFilename(filename);
                deposit.setInputStream(mediaPart);
                deposit.setMimeType(mimeType);
                deposit.setMd5(md5);
                deposit.setPackaging(packaging);
            }
        }

        try {
            this.storeAndCheckBinary(deposit, this.config);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        }
    }
    
    protected void cleanup(final Deposit deposit) {
        if (deposit == null) {
            return;
        }

        File tmp = deposit.getFile();
        if (tmp == null) {
            return;
        }

        if (!tmp.delete()) {
            log.error("Could not delete temporary deposit file " + tmp.getAbsolutePath());
        }
        deposit.setFile(null);
    }

    protected Element getGenerator(final SwordConfiguration config) {
        String generatorUri = config.generator();
        String generatorVersion = config.generatorVersion();
        String adminEmail = config.administratorEmail();
        if (generatorUri != null && !"".equals(generatorUri)) {
            Abdera abdera = new Abdera();
            Element generator = abdera.getFactory().newGenerator();
            generator.setAttributeValue("uri", generatorUri);
            if (generatorVersion != null) {
                generator.setAttributeValue("version", generatorVersion);
            }
            if (adminEmail != null && !"".equals(adminEmail)) {
                generator.setText(adminEmail);
            }
            return generator;
        }
        return null;
    }

    protected void addDepositPropertiesFromEntry(final Deposit deposit, final HttpServletRequest req) throws IOException, SwordError {
        parseEntryFromInputStream(deposit, req.getInputStream());
    }
    
    /**
     * Parsing the Entry element from an input Stream (either from the HTTP request or from the multipart upload)
     * and stuffing it into the given Deposit.
     * @param deposit - the {@link Deposit} to stuff the {@link Entry} into.
     * @param entryPart - the {@link InputStream} to read from
     * @throws SwordError when parsing fails.
     */
    private void parseEntryFromInputStream(final Deposit deposit, final InputStream entryPart) throws SwordError {
        Abdera abdera = new Abdera();
        Parser parser = abdera.getParser();
        Document<Entry> entryDoc = null;
        try {
            entryDoc = parser.parse(entryPart);
        } catch (ParseException ex) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Unable to parse SWORD entry", ex);
        }
        Entry entry = entryDoc.getRoot();
        deposit.setEntry(entry);
    }
    
    protected void addDepositPropertiesFromBinary(final Deposit deposit, final HttpServletRequest req) throws ServletException, IOException, SwordError {
        String contentType = this.getContentType(req);
        String contentDisposition = req.getHeader("Content-Disposition");
        String md5 = req.getHeader("Content-MD5");
        String packaging = req.getHeader("Packaging");
        if (packaging == null || "".equals(packaging)) {
            packaging = UriRegistry.PACKAGE_BINARY;
        }
        long len = -1L;
        if (req.getHeader("Content-Length") != null) {
            try {
                len = Long.parseLong(req.getHeader("Content-Length"));
            } catch (NumberFormatException e) { }
        }

        InputStream file = req.getInputStream();

        // now let's interpret and deal with the headers that we have
        String filename = this.getContentDispositionValue(contentDisposition, "filename");
        if (filename == null || "".equals(filename)) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Filename could not be extracted from Content-Disposition");
        }

        deposit.setFilename(filename);
        deposit.setMd5(md5);
        deposit.setPackaging(packaging);
        deposit.setInputStream(file);
        deposit.setMimeType(contentType);
        deposit.setContentLength(len);

        try {
            this.storeAndCheckBinary(deposit, this.config);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        }
    }

    protected void swordError(final HttpServletRequest req, final HttpServletResponse resp, final SwordError e) throws IOException, ServletException {
        try {
            if (!this.config.returnErrorBody() || !e.hasBody()) {
                ErrorDocument doc = new ErrorDocument(e.getErrorUri(), e.getStatus());
                resp.setStatus(doc.getStatus());
                return;
            }

            // treatment is either the default value in the ErrorDocument OR the error message if it exists
            String treatment = e.getMessage();

            // verbose description is the stack trace if allowed, otherwise null
            String verbose = null;
            if (this.config.returnStackTraceInError()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                verbose = sw.getBuffer().toString();
            }

            ErrorDocument doc;
            if (treatment == null) {
                doc = new ErrorDocument(e.getErrorUri(), e.getStatus(), verbose);
            } else {
                doc = new ErrorDocument(e.getErrorUri(), e.getStatus(), treatment, verbose);
            }

            // now write the response
            resp.setStatus(doc.getStatus());
            resp.setHeader("Content-Type", "text/xml");

            doc.writeTo(resp.getWriter(), this.config);
            resp.getWriter().flush();
        } catch (SwordServerException sse) {
            throw new ServletException(sse);
        }
    }

    protected String getContentDispositionValue(final String contentDisposition, final String key) {
        if (contentDisposition == null || key == null) {
            return null;
        }

        ParameterParser parameterParser = new ParameterParser();
        char separator = ';';
        Map<String, String> parameters = parameterParser.parse(contentDisposition, separator);
        return parameters.get(key);
    }

    protected List<FileItem> getPartsFromRequest(final HttpServletRequest request) throws ServletException {
        try {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            List<FileItem> items = upload.parseRequest(request);

            return items;
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }

    protected Map<String, String> getAcceptHeaders(final HttpServletRequest req) {
        Map<String, String> acceptHeaders = new HashMap<String, String>();
        Enumeration headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            if (header.toLowerCase().startsWith("accept")) {
                acceptHeaders.put(header, req.getHeader(header));
            }
        }
        return acceptHeaders;
    }

    protected void copyInputToOutput(final InputStream in, final OutputStream out) throws IOException {
        final int bufferSize = 1024 * 4;
        final byte[] buffer = new byte[bufferSize];

        while (true) {
            final int count = in.read(buffer, 0, bufferSize);

            if (-1 == count) {
                break;
            }

            // write out those same bytes
            out.write(buffer, 0, count);
        }
    }

    protected String getContentType(final HttpServletRequest req) {
        String contentType = req.getHeader("Content-Type");
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    protected boolean getInProgress(final HttpServletRequest req) throws SwordError {
        String iph = req.getHeader("In-Progress");
        boolean inProgress = false; // default value
        if (iph != null) {
            // first of all validate that the value is "true" or "false"
            if (!"true".equals(iph.trim()) && !"false".equals(iph.trim())) {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "The In-Progress header MUST be 'true' or 'false'");
            }
            inProgress = "true".equals(iph.trim());
        }
        return inProgress;
    }

    protected boolean getMetadataRelevant(final HttpServletRequest req) throws SwordError {
        String mdr = req.getHeader("Metadata-Relevant");
        boolean metadataRelevant = false; // default value
        if (mdr != null) {
            // first of all validate that the value is "true" or "false"
            if (!"true".equals(mdr.trim()) && !"false".equals(mdr.trim())) {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "The In-Progress header MUST be 'true' or 'false'");
            }
            metadataRelevant = "true".equals(mdr.trim());
        }
        return metadataRelevant;
    }
}
