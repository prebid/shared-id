package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ExtraHeadersCookie;
import org.sharedid.endpoint.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CheckOptOutHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(CheckOptOutHandler.class);

    private String optOutCookieValue;
    private String sharedIdCookieName;
    private Long sharedIdOptOutTtl;
    private Boolean isSecureCookiesEnabled;

    @Autowired
    public CheckOptOutHandler(@Value("${cookie.shared-id.opt-out-value}") String optOutCookieValue,
                              @Value("${cookie.shared-id.name}") String sharedIdCookieName,
                              @Value("${cookie.shared-id.opt-out-ttl}") Long sharedIdOptOutTtl,
                              @Value("${cookies.secure}") Boolean isSecureCookiesEnabled) {
        this.optOutCookieValue = optOutCookieValue;
        this.sharedIdCookieName = sharedIdCookieName;
        this.sharedIdOptOutTtl = sharedIdOptOutTtl;
        this.isSecureCookiesEnabled = isSecureCookiesEnabled;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String userId = dataContext.getUserId();

        boolean hasOptedOutUserId = optOutCookieValue.equals(userId);

        if (hasOptedOutUserId) {
            logger.debug("Is opted out");
            //update opt out cookie ttl
            updateOptOutCookie(routingContext);
            ResponseUtil.noContent(routingContext.response());
            return;
        }

        routingContext.next();
    }

    private void updateOptOutCookie(RoutingContext routingContext) {
        Cookie responseCookie = ExtraHeadersCookie.fromCookie(Cookie.cookie(sharedIdCookieName, optOutCookieValue));
        responseCookie.setMaxAge(sharedIdOptOutTtl);
        responseCookie.setSecure(isSecureCookiesEnabled);

        routingContext.response().addCookie(responseCookie);
    }
}
