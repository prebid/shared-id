package org.sharedid.endpoint.util;

import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RequestAddressUtil {
    private static Logger logger = LoggerFactory.getLogger(RequestAddressUtil.class);

    private RequestAddressUtil() {}

    //logic for converting vertx request to an address...
    public static InetAddress getInetAddress(HttpServerRequest request) {
        String remoteAddress = getRemoteAddress(request);

        try {
            if (remoteAddress != null && !"".equals(remoteAddress)) {

                if (remoteAddress.contains(",")) {
                    String[] ip = remoteAddress.split(",");
                    logger.debug("Multiple IPs in header {} using {}", remoteAddress, ip[0]);

                    remoteAddress = ip[0].trim();
                }

                logger.debug("Remote Address used {}", remoteAddress);
                return InetAddress.getByName(remoteAddress);
            }
        } catch (UnknownHostException e) {
            logger.error("Unknown Host {}", remoteAddress);
        }

        return null;
    }

    private static String getRemoteAddress(HttpServerRequest request) {
        String xForwardAddress = request.getHeader("X-FORWARDED-FOR");

        if (StringUtils.isBlank(xForwardAddress)) {
            xForwardAddress = request.getHeader("X-Forwarded-For");
        }

        if (StringUtils.isBlank(xForwardAddress)) {
            xForwardAddress = request.getHeader("x-forwarded-for");
        }

        if (StringUtils.isNotBlank(xForwardAddress)) {
            logger.debug("Using header X-FORWARDED-FOR IP - {} ", xForwardAddress);
            return xForwardAddress;
        }

        String forwardedAddress = request.getHeader("FORWARDED");
        if (StringUtils.isNotBlank(forwardedAddress)) {
            logger.debug("Using header FORWARDED IP - {} ", forwardedAddress);
            return forwardedAddress;
        }

        String remoteAddress = request.remoteAddress().host();
        logger.debug("No proxy header - Using request IP - {} ", remoteAddress);

        return remoteAddress;
    }
}
