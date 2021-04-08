package org.sharedid.endpoint.config;

import io.vertx.core.Verticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.sharedid.endpoint.verticle.HttpServerVerticle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class HttpConfiguration {

    @ConfigurationProperties("vertx.http-server")
    @Bean
    public HttpServerOptions httpServerOptions() {
        return new HttpServerOptions();
    }

    @Bean
    public Supplier<Verticle> httpServerVerticleSupplier(
            HttpServerOptions httpServerOptions,
            Router router) {
        return () -> new HttpServerVerticle(httpServerOptions, router);
    }
}
