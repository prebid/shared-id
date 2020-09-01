package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.model.AuditCookie;
import org.sharedid.endpoint.model.AuditCookieSerializationException;
import org.sharedid.endpoint.service.AuditCookieService;
import org.sharedid.endpoint.service.LocationService;
import org.sharedid.endpoint.util.ExtraHeadersCookie;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.util.RequestAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.time.Instant;

@Component
public class SetAuditCookieHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SetAuditCookieHandler.class);

    public static final String METRIC_CREATE_AUDIT_COOKIE = "shared-id.handler.audit.create_audit_cookie";
    public static final String METRIC_CREATE_AUDIT_COOKIE_FAILED = "shared-id.handler.audit.create_audit_cookie_failed";
    public static final String METRIC_RENEW_AUDIT_COOKIE = "shared-id.handler.audit.renew_audit_cookie";
    public static final String METRIC_SERIALIZE_AUDIT_COOKIE_FAILED =
            "shared-id.handler.audit.serialize_audit_cookie_failed";

    private LocationService locationService;
    private AuditCookieService auditCookieService;
    private String auditCookieName;
    private long auditCookieTtl;
    private String cipherKey;
    private Boolean isSecureCookiesEnabled;

    private Meter createAuditCookieMeter;
    private Meter createAuditCookieFailedMeter;
    private Meter renewAuditCookieMeter;
    private Meter serializeAuditCookieFailedMeter;

    @Autowired
    public SetAuditCookieHandler(LocationService locationService,
                                 AuditCookieService auditCookieService,
                                 MetricRegistry metricRegistry,
                                 @Value("${cookie.audit.name}") String auditCookieName,
                                 @Value("${cookie.audit.ttl}") long auditCookieTtl,
                                 @Value("${cookie.audit.cipher-key}") String cipherKey,
                                 @Value("${cookies.secure}") Boolean isSecureCookiesEnabled) {
        this.locationService = locationService;
        this.auditCookieService = auditCookieService;
        this.auditCookieName = auditCookieName;
        this.auditCookieTtl = auditCookieTtl;
        this.cipherKey = cipherKey;
        this.isSecureCookiesEnabled = isSecureCookiesEnabled;

        this.createAuditCookieMeter = metricRegistry.meter(METRIC_CREATE_AUDIT_COOKIE);
        this.createAuditCookieFailedMeter = metricRegistry.meter(METRIC_CREATE_AUDIT_COOKIE_FAILED);
        this.renewAuditCookieMeter = metricRegistry.meter(METRIC_RENEW_AUDIT_COOKIE);
        this.serializeAuditCookieFailedMeter = metricRegistry.meter(METRIC_SERIALIZE_AUDIT_COOKIE_FAILED);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        AuditCookie auditCookie = dataContext.getAuditCookie();

        Future<AuditCookie> auditCookieFuture;
        if (auditCookie == null) {
            logger.debug("Creating audit cookie");
            createAuditCookieMeter.mark();
            auditCookieFuture = getNewAuditCookie(dataContext, routingContext);
        } else {
            logger.debug("Renewing audit cookie");
            renewAuditCookieMeter.mark();
            auditCookie.setRenewedTimestampSeconds(Instant.now().toEpochMilli());
            auditCookieFuture = Future.succeededFuture(auditCookie);
        }

        auditCookieFuture.onComplete(result -> {
            if (result.failed()) {
                logger.error("Failed to create an audit cookie", result.cause());
                createAuditCookieFailedMeter.mark();
                routingContext.next();
                return;
            }

            try {
                Cookie cookie = result.result().toCookie(auditCookieName, cipherKey, auditCookieTtl);

                boolean isSecure;
                if (isSecureCookiesEnabled != null) {
                    isSecure = isSecureCookiesEnabled;
                } else {
                    isSecure = routingContext.request().scheme().equals("https");
                }

                cookie.setSecure(isSecure);

                routingContext.addCookie(ExtraHeadersCookie.fromCookie(cookie));
            } catch (AuditCookieSerializationException e) {
                logger.debug("Failed to serialize audit cookie {} {}", result.result(), e.cause.getMessage());
                serializeAuditCookieFailedMeter.mark();
            }

            routingContext.next();
        });
    }

    private Future<AuditCookie> getNewAuditCookie(DataContext dataContext, RoutingContext routingContext) {
        String geoQuery = dataContext.getGeoQuery();

        Future<String> geoQueryResult;

        if (geoQuery != null) {
            geoQueryResult = Future.succeededFuture(geoQuery);
        } else {
            geoQueryResult = locationService.getCountryForRequest(routingContext.request());
        }

        return geoQueryResult.map(countryCode -> {
            String userId = dataContext.getUserId();
            String gdprConsentParam = dataContext.getGdprConsentParam();
            return auditCookieService
                    .createAuditCookie(routingContext.request(), userId, countryCode, gdprConsentParam);
        });
    }
}
