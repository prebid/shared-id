package org.sharedid.endpoint.handler;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AddRequestMetricsHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(AddRequestMetricsHandler.class);

    private static final String METRIC_REQUEST_TIME = "shared-id.handler.request.request_time";
    private static final String METRIC_REQUEST_ROUTE_TIME_TEMPLATE = "shared-id.handler.request.route.%s.time";

    private MetricRegistry metricRegistry;

    private Timer requestTimer;

    @Autowired
    public AddRequestMetricsHandler(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.requestTimer = metricRegistry.timer(METRIC_REQUEST_TIME);
    }

    @PostConstruct
    public void init() {
        requestTimer = metricRegistry.timer(METRIC_REQUEST_TIME);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        String route = request.path().replace("/", "");

        logger.info("Accepted request {} {}", request.method(), request.uri());

        Timer.Context timer = requestTimer.time();

        Timer.Context routeTimer =
                metricRegistry.timer(String.format(METRIC_REQUEST_ROUTE_TIME_TEMPLATE, route)).time();

        response.bodyEndHandler(v -> {
            logger.info("Response sent for {}", routingContext.request().uri());

            timer.stop();
            routeTimer.stop();
        });

        routingContext.next();
    }
}
