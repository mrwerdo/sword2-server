package org.swordapp.server;

public final class SwordAuthException extends Exception {
    private boolean retry = false;

    public SwordAuthException() {
        super();
    }

    public SwordAuthException(final boolean retry) {
        super();
        this.retry = retry;
    }

    public SwordAuthException(final String message) {
        super(message);
    }

    public SwordAuthException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SwordAuthException(final Throwable cause) {
        super(cause);
    }

    public boolean isRetry() {
        return retry;
    }
}
