package org.swordapp.server;

import java.io.InputStream;
import java.util.Date;

public class MediaResource {
    private String packaging = UriRegistry.PACKAGE_SIMPLE_ZIP;
    private String contentType = "application/octet-stream";
    private InputStream inputStream = null;
    private boolean unpackaged = false;
    private String contentMD5;
    private Date lastModified;

    public MediaResource(final InputStream in, final String contentType, final String packaging) {
        this(in, contentType, packaging, false);
    }

    public MediaResource(final InputStream in, final String contentType, final String packaging, final boolean unpackaged) {
        this.inputStream = in;
        this.contentType = contentType;
        this.packaging = packaging;
        this.unpackaged = unpackaged;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public Date getLastModified() {
        return lastModified == null ? null : new Date(lastModified.getTime());
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = new Date(lastModified.getTime());
    }

    public boolean isUnpackaged() {
        return unpackaged;
    }

    public void setUnpackaged(final boolean unpackaged) {
        this.unpackaged = unpackaged;
    }

    public String getPackaging() {
        return packaging;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setPackaging(final String packaging) {
        this.packaging = packaging;
    }
}
