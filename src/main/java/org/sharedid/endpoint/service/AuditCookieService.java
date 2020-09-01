package org.sharedid.endpoint.service;

import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.model.AuditCookie;
import org.sharedid.endpoint.util.HostUtils;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Instant;

@Component
public class AuditCookieService {
    public AuditCookie createAuditCookie(HttpServerRequest request,
                                         String userId,
                                         String geoQuery,
                                         String gdprConsentString) {
        String version = "1";

        String hostIpAddress = null;
        InetAddress localHost = HostUtils.getLocalhost();
        if (localHost != null) {
            hostIpAddress = localHost.getHostAddress();
        }

        String userIpAddress = HostUtils.getUserAddress(request);

        String countryCode = null;
        if (geoQuery != null) {
            countryCode = geoQuery;
        }

        long renewedTimestampSeconds = Instant.now().toEpochMilli();
        String referrerDomain = request.uri();

        String initiatorId = "";
        String initiatorType = "";

        boolean hasConsentString = StringUtils.isNotEmpty(gdprConsentString);

        return new AuditCookie(
                version,
                userId,
                hostIpAddress,
                userIpAddress,
                countryCode,
                renewedTimestampSeconds,
                referrerDomain,
                initiatorType,
                initiatorId,
                hasConsentString,
                gdprConsentString
        );
    }
}
