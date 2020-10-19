package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class RespondHealthCheckHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.end();
    }
}
