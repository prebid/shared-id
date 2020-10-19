package org.sharedid.endpoint.model;

public class AuditCookieSerializationException extends Exception {
    public final Throwable cause;

    public AuditCookieSerializationException(Throwable cause)
    {
        this.cause = cause;
    }
}
