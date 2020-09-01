package org.sharedid.endpoint.handler;

import de.huxhorn.sulky.ulid.ULID;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.context.DataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReadSharedIdHandler implements Handler<RoutingContext> {
    private String sharedIdCookieName;
    private String myAdsCookieName;

    private ULID ulidGenerator = new ULID();

    @Autowired
    public ReadSharedIdHandler(@Value("${cookie.shared-id.name}") String sharedIdCookieName,
                               @Value("${cookie.myads.name}") String myAdsCookieName) {
        this.sharedIdCookieName = sharedIdCookieName;
        this.myAdsCookieName = myAdsCookieName;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String userId = null;

        Cookie cookie = routingContext.request().getCookie(sharedIdCookieName);

        if (cookie != null) {
            userId = cookie.getValue();
            dataContext.setUserCookie(cookie);
        } else {
            Cookie myAdsCookie = routingContext.request().getCookie(myAdsCookieName);

            if (myAdsCookie != null) {
                userId = myAdsCookie.getValue();
                dataContext.setUserCookie(myAdsCookie);
            }
        }

        if (isValid(userId)) {
            dataContext.setUserId(userId);
            dataContext.setIsNewUserId(false);
        } else {
            String newUserId = generateUserId();
            dataContext.setUserId(newUserId);
            dataContext.setIsNewUserId(true);
        }

        routingContext.next();
    }

    private boolean isValid(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        int length = userId.length();

        if (length == 26) {
            return StringUtils.isAlphanumeric(userId);
        }

        if (length == 36) {
            //is legacy
            return true;
        }

        return false;
    }

    private String generateUserId() {
        return ulidGenerator.nextULID();
    }
}
