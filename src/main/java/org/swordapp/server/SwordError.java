package org.swordapp.server;

public final class SwordError extends Exception {
    private String errorUri;
    private int status = -1;
    private boolean hasBody = true;

    public SwordError() {
        super();
    }

    public SwordError(final int status) {
        super();
        this.status = status;
        this.hasBody = false;
    }

    public SwordError(final String errorUri) {
        super(errorUri);
        this.errorUri = errorUri;
    }

    public SwordError(final String errorUri, final int status) {
        super(errorUri);
        this.errorUri = errorUri;
        this.status = status;
    }

    public SwordError(final String errorUri, final String message) {
        super(message);
        this.errorUri = errorUri;
    }

    public SwordError(final String errorUri, final int status, final String message) {
        super(message);
        this.errorUri = errorUri;
        this.status = status;
    }

    public SwordError(final String errorUri, final Throwable cause) {
        super(errorUri, cause);
        this.errorUri = errorUri;
    }

    public SwordError(final String errorUri, final int status, final Throwable cause) {
        super(errorUri, cause);
        this.errorUri = errorUri;
        this.status = status;
    }

    public SwordError(final String errorUri, final String message, final Throwable cause) {
        super(message, cause);
        this.errorUri = errorUri;
    }

    public SwordError(final String errorUri, final int status, final String message, final Throwable cause) {
        super(message, cause);
        this.errorUri = errorUri;
        this.status = status;
    }

    public String getErrorUri() {
        return errorUri;
    }

    public int getStatus() {
        return status;
    }

    public boolean hasBody() {
        return hasBody;
    }
}
