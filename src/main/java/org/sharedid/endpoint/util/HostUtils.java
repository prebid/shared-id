package org.sharedid.endpoint.util;

import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class HostUtils {

    private static final Logger logger = LoggerFactory.getLogger(HostUtils.class);

    private HostUtils() {
    }

    @SuppressWarnings("squid:S1166")
    public static InetAddress getLocalhost() {
        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            logger.debug("Could not retrieve host ip. {}", e.getMessage());
            return null;
        }
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            throw new UnsupportedOperationException("Failed to get hostname", e);
        }
    }

    public static String getUserAddress(HttpServerRequest request) {
        if (request == null) {
            return null;
        }

        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr != null) {
            return remoteAddr;
        }

        remoteAddr = request.getHeader("FORWARDED");
        if (remoteAddr != null) {
            return remoteAddr;
        }
        return request.remoteAddress().host();
    }
}
