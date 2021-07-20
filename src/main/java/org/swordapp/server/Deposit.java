package org.swordapp.server;

import org.apache.abdera.model.Entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Deposit {
    private SwordEntry entry = null;
    private InputStream inputStream = null;
    private String filename;
    private String mimeType;
    private String slug = null;
    private String md5 = null;
    private String packaging;
    private boolean inProgress = false;
    private boolean metadataRelevant = true;
    private File file = null;
    private long contentLength = -1L;

    public Deposit() { }

    public Deposit(final Entry entry, final InputStream inputStream, final String filename, final String mimeType,
                   final String slug, final String md5, final String packaging, final boolean inProgress) {
        this.entry = new SwordEntry(entry);
        this.inputStream = inputStream;
        this.filename = filename;
        this.mimeType = mimeType;
        this.slug = slug;
        this.md5 = md5;
        this.packaging = packaging;
        this.inProgress = inProgress;
    }

    public boolean isEntryOnly() {
        return this.entry != null && this.inputStream == null && this.file == null;
    }

    public boolean isMultipart() {
        return this.entry != null && (this.inputStream != null || this.file != null);
    }

    public boolean isBinaryOnly() {
        return this.entry == null && (this.inputStream != null || this.file != null);
    }

    public File getFile() {
        return file;
    }

    public void setFile(final File file) {
        this.file = file;
        this.inputStream = null;
    }

    public SwordEntry getSwordEntry() {
        return entry;
    }

    public void setEntry(final Entry entry) {
        this.entry = new SwordEntry(entry);
    }

    public InputStream getInputStream() throws SwordServerException {
        try {
            if (inputStream == null && file == null) {
                return null;
            } else if (inputStream == null && file != null) {
                return new FileInputStream(this.file);
            } else if (inputStream != null) {
                return inputStream;
            }
            return null;
        } catch (IOException e) {
            throw new SwordServerException(e);
        }
    }

    public void setInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(final String md5) {
        this.md5 = md5;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(final String packaging) {
        this.packaging = packaging;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(final boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isMetadataRelevant() {
        return metadataRelevant;
    }

    public void setMetadataRelevant(final boolean metadataRelevant) {
        this.metadataRelevant = metadataRelevant;
    }

    public void setContentLength(final long length) {
        this.contentLength = length;
    }

    public long getContentLength() {
        return contentLength;
    }
}
