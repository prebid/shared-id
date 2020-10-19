package org.sharedid.endpoint.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.impl.cpu.CpuCoreSensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.function.Supplier;

@Configuration
public class VerticleConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(VerticleConfiguration.class);

    @Autowired
    public Vertx vertx;

    @Autowired
    public Supplier<Verticle> httpServerVerticleSupplier;

    @PostConstruct
    public void deployVerticles() {
        int instances = CpuCoreSensor.availableProcessors() * 2;

        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(instances);

        vertx.deployVerticle(httpServerVerticleSupplier, deploymentOptions, result -> {
            if (result.succeeded()) {
                logger.debug("Deployed HttpServerVerticle. Instances {}", instances);
            } else {
                logger.error("Failed to deploy HttpServerVerticle", result.cause());
                throw new IllegalStateException("Failed to deploy HttpServerVerticle");
            }
        });
    }
}
