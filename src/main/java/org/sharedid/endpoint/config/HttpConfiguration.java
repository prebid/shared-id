package org.sharedid.endpoint.config;

import org.sharedid.endpoint.verticle.HttpServerVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class HttpConfiguration {

    @Bean
    public HttpServerOptions httpServerOptions() {
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setPort(80);
        return httpServerOptions;
    }

    @Bean
    public Supplier<Verticle> httpServerVerticleSupplier(Vertx vertx,
                                                         HttpServerOptions httpServerOptions,
                                                         Router router) {
        return () -> new HttpServerVerticle(httpServerOptions, router);
    }
}
