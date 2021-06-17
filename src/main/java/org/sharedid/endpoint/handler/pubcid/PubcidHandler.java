package org.sharedid.endpoint.handler.pubcid;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.util.IdGeneratorUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PubcidHandler implements Handler<RoutingContext> {
    private static final String SOURCE = "pubcid.org";

    @Override
    public void handle(RoutingContext event) {
        HttpServerResponse response = event.response();
        String id = IdGeneratorUtils.generatePubcid();
        JsonObject responseJson = new JsonObject(Map.of(SOURCE, id));
        response.setStatusCode(200);
        response.end(responseJson.toBuffer());
    }
}
