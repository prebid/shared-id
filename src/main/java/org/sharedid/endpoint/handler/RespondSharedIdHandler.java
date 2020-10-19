package org.sharedid.endpoint.handler;

import org.sharedid.endpoint.context.DataContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RespondSharedIdHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(RespondSharedIdHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        HttpServerResponse response = routingContext.response();

        String userId = dataContext.getUserId();

        JsonObject responseJson = new JsonObject();
        responseJson.put("sharedId", userId);

        response.setStatusCode(200);

        logger.debug("Responding with {} {}", response.getStatusCode(), responseJson);

        response.end(responseJson.toBuffer());
    }
}
