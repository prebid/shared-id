package org.sharedid.endpoint.model;

public class AuditCookieDeserializationException extends Exception {
    public final Throwable cause;

    public AuditCookieDeserializationException(Throwable cause)
    {
        this.cause = cause;
    }
}
