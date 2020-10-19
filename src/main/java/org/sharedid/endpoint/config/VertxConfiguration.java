package org.sharedid.endpoint.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertxConfiguration {
    @Bean
    public Vertx vertx() {
        VertxOptions vertxOptions = new VertxOptions();
        return Vertx.vertx(vertxOptions);
    }
}
