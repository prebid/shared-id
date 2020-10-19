package org.sharedid.endpoint.consent;

public class GdprConsentStringException extends Exception {
    public String rawConsentString;

    public GdprConsentStringException(String message, String rawConsentString) {
        super(message);
        this.rawConsentString = rawConsentString;
    }

    public String getRawConsentString() {
        return rawConsentString;
    }
}
