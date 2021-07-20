package org.swordapp.server;

public final class SwordServerException extends Exception {
    public SwordServerException() {
        super();
    }

    public SwordServerException(final String message) {
        super(message);
    }

    public SwordServerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SwordServerException(final Throwable cause) {
        super(cause);
    }
}
