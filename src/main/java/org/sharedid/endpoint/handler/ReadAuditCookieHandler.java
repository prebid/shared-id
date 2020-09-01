package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.model.AuditCookie;
import org.sharedid.endpoint.model.AuditCookieDeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReadAuditCookieHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(ReadAuditCookieHandler.class);

    private String auditCookieName;
    private String auditCookieCipherKey;

    @Autowired
    public ReadAuditCookieHandler(@Value("${cookie.audit.name}") String auditCookieName,
                                  @Value("{cookie.audit.cipher-key}") String auditCookieCipherKey) {
        this.auditCookieName = auditCookieName;
        this.auditCookieCipherKey = auditCookieCipherKey;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        Cookie cookie = routingContext.getCookie(auditCookieName);

        if (cookie == null) {
            routingContext.next();
            return;
        }

        try {
            AuditCookie auditCookie = AuditCookie.fromCookie(cookie, auditCookieCipherKey);
            dataContext.setAuditCookie(auditCookie);
        } catch (AuditCookieDeserializationException e) {
            logger.debug("Failed to deserialize audit cookie {} {}", cookie.getValue(), e);
        }

        routingContext.next();
    }
}
