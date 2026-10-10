package com.mbu.dslibrary.exception;

/** An outside service (such as GitHub) could not answer. The message is safe to show to the user. */
public class UpstreamException extends RuntimeException {
    private final int status;

    public UpstreamException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
