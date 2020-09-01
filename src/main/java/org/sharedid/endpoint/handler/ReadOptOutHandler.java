package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.context.DataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReadOptOutHandler implements Handler<RoutingContext> {
    private String optOutCookieName;
    private String legacyOptOutCookieName;

    @Autowired
    public ReadOptOutHandler(@Value("${cookie.opt-out.name}") String optOutCookieName,
                             @Value("${cookie.opt-out.legacy}") String legacyOptOutCookieName) {
        this.optOutCookieName = optOutCookieName;
        this.legacyOptOutCookieName = legacyOptOutCookieName;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        boolean hasOptOutCookie = getOptOutStatus(routingContext);

        dataContext.setHasOptOutCookie(hasOptOutCookie);

        routingContext.next();
    }

    private boolean getOptOutStatus(RoutingContext routingContext) {
        Cookie optOutCookie = routingContext.getCookie(optOutCookieName);
        if (optOutCookie != null) {
            return true;
        }

        Cookie legacyOptOutCookie = routingContext.getCookie(legacyOptOutCookieName);
        if (legacyOptOutCookie != null) {
            return true;
        }

        return false;
    }
}
