package org.sharedid.endpoint.util;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

public class ExtraHeadersCookie extends CookieImpl {

    private long maxAge;

    public ExtraHeadersCookie(String name, String value) {
        super(name, value);
    }

    public static ExtraHeadersCookie fromCookie(Cookie cookie) {
        ExtraHeadersCookie extraHeadersCookie =
                new ExtraHeadersCookie(cookie.getName(), cookie.getValue());
        extraHeadersCookie.setDomain(cookie.getDomain());
        extraHeadersCookie.setPath(cookie.getPath());
        return extraHeadersCookie;
    }

    public long getMaxAge() {
        return maxAge;
    }

    @Override
    public Cookie setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        super.setMaxAge(maxAge);
        return this;
    }

    @Override
    public String encode() {
        String encoded = super.encode();
        //add extra headers
        encoded += "; SameSite=None";
        return encoded;
    }
}
