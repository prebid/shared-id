package org.sharedid.endpoint.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class HttpServerVerticle extends AbstractVerticle {
    private HttpServerOptions options;
    private Router router;

    public HttpServerVerticle(HttpServerOptions options, Router router) {
        this.options = options;
        this.router = router;
    }

    @Override
    public void start() {
        vertx.createHttpServer(options).requestHandler(router).listen();
    }
}
