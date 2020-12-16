package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.context.DataContext;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.util.ExtraHeadersCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SetSharedIdCookieHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SetSharedIdCookieHandler.class);

    private static final String METRIC_NEW_USER_ID = "shared-id.handler.shared-id.new_user_id";
    private static final String METRIC_EXISTING_USER_ID = "shared-id.handler.shared-id.existing_user_id";
    private static final String METRIC_SYNCED_USER_ID = "shared-id.handler.shared-id.synced_user_id";

    private String cookieName;
    private long cookieTtl;
    private Boolean isSecureCookiesEnabled;
    private Boolean isHttpOnlyCookiesEnabled;

    private Meter newUserIdMeter;
    private Meter existingUserIdMeter;
    private Meter syncedUserIdMeter;

    @Autowired
    public SetSharedIdCookieHandler(@Value("${cookie.shared-id.name}") String cookieName,
                                    @Value("${cookie.shared-id.ttl}") long cookieTtl,
                                    @Value("${cookies.secure}") Boolean isSecureCookiesEnabled,
                                    MetricRegistry metricRegistry,
                                    @Value("${cookies.httpOnly:true}") Boolean isHttpOnlyCookiesEnabled) {
        this.cookieName = cookieName;
        this.cookieTtl = cookieTtl;
        this.isSecureCookiesEnabled = isSecureCookiesEnabled;
        this.isHttpOnlyCookiesEnabled = isHttpOnlyCookiesEnabled;

        this.newUserIdMeter = metricRegistry.meter(METRIC_NEW_USER_ID);
        this.existingUserIdMeter = metricRegistry.meter(METRIC_EXISTING_USER_ID);
        this.syncedUserIdMeter = metricRegistry.meter(METRIC_SYNCED_USER_ID);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        Boolean isNewUserId = dataContext.isNewUserId();
        if (isNewUserId == null) {
            isNewUserId = false;
        }

        boolean isSyncedUserId = dataContext.isSyncedUserId();

        String userId = dataContext.getUserId();

        if (isSyncedUserId) {
            logger.debug("Syncing new user id {}", userId);
            syncedUserIdMeter.mark();
        } else if (isNewUserId) {
            logger.debug("Setting new user id {}", userId);
            newUserIdMeter.mark();
        } else {
            logger.debug("Setting existing user id {}", userId);
            existingUserIdMeter.mark();
        }

        boolean isSecure;
        if (isSecureCookiesEnabled != null) {
            isSecure = isSecureCookiesEnabled;
        } else {
            isSecure = routingContext.request().scheme().equals("https");
        }

        Cookie cookie = ExtraHeadersCookie.fromCookie(Cookie.cookie(cookieName, userId));
        cookie.setMaxAge(cookieTtl);
        cookie.setSecure(isSecure);
        cookie.setHttpOnly(isHttpOnlyCookiesEnabled);

        routingContext.addCookie(cookie);

        routingContext.next();
    }
}
