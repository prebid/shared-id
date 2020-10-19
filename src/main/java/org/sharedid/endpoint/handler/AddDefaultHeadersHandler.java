package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.springframework.stereotype.Component;

@Component
public class AddDefaultHeadersHandler implements Handler<RoutingContext> {
    public static final String P3P_HEADER = "P3P";
    public static final String PRAGMA_HEADER = "Pragma";
    public static final String CACHE_CONTROL_HEADER = "Cache-Control";
    public static final String EXPIRES_HEADER = "Expires";

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        response.putHeader(P3P_HEADER, "CP=\"NOI CURa ADMa DEVa TAIa OUR BUS IND UNI COM NAV INT\"");
        response.putHeader(PRAGMA_HEADER, "no-cache");
        response.putHeader(CACHE_CONTROL_HEADER, "no-cache,no-store,must-revalidate");
        response.putHeader(EXPIRES_HEADER, "0");

        routingContext.next();
    }
}
