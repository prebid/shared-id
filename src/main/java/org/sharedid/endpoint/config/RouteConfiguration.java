package org.sharedid.endpoint.config;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import org.sharedid.endpoint.handler.*;
import org.sharedid.endpoint.handler.pubcid.PubcidHandler;
import org.sharedid.endpoint.util.HandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RouteConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RouteConfiguration.class);

    @Value("${templates.opt-out}")
    private String optOutTemplate;

    @Value("${templates.opt-in}")
    private String optInTemplate;

    @Bean
    public HandlerRegistry handlerRegistry(List<Handler<RoutingContext>> handlers) {
        return new HandlerRegistry(handlers);
    }

    @Bean
    public TemplateEngine templateEngine(Vertx vertx) {
        return HandlebarsTemplateEngine.create(vertx);
    }

    @Bean
    public TemplateHandler templateHandler(TemplateEngine templateEngine,
                                           @Value("${handlers.template.template-dir}") String templateDirectory) {
        return TemplateHandler.create(templateEngine, templateDirectory, "text/html");
    }

    @Bean
    public Router router(Vertx vertx, HandlerRegistry handlerRegistry, TemplateEngine templateEngine) {
        Router router = Router.router(vertx);

        router.route("/*")
                .handler(handlerRegistry.get(AddRequestMetricsHandler.class))
                .handler(
                        CorsHandler.create(".*.")
                                .allowCredentials(true)
                                .allowedHeader("Access-Control-Allow-Method")
                                .allowedHeader("Access-Control-Allow-Origin")
                                .allowedHeader("Access-Control-Allow-Credentials")
                                .allowedHeader("Content-type")
                                .allowedMethod(HttpMethod.GET)
                                .allowedMethod(HttpMethod.POST)
                                .allowedMethod(HttpMethod.OPTIONS));

        router.get("/lib/*").handler(StaticHandler.create("webroot/lib"));
        router.get("/keys/*").handler(StaticHandler.create("webroot/keys"));
        router.get("/").handler(StaticHandler.create().setIndexPage("index.html"));

        router.get("/usync")
                .handler(handlerRegistry.get(AddDefaultHeadersHandler.class))
                .handler(handlerRegistry.get(ReadParametersHandler.class))
                .handler(handlerRegistry.get(ReadSharedIdHandler.class))
                .handler(handlerRegistry.get(CheckOptOutHandler.class))
                .handler(handlerRegistry.get(SetLocationHandler.class))
                .handler(handlerRegistry.get(ParseGdprConsentStringHandler.class))
                .handler(handlerRegistry.get(ReadAuditCookieHandler.class))
                .handler(handlerRegistry.get(CheckGdprConsentHandler.class))
                .handler(handlerRegistry.get(CheckCcpaConsentHandler.class))
                .handler(handlerRegistry.get(CheckVendorHandler.class))
                .handler(handlerRegistry.get(SetAuditCookieHandler.class))
                .handler(handlerRegistry.get(SetSharedIdCookieHandler.class))
                .handler(handlerRegistry.get(RedirectHandler.class))
                .handler(handlerRegistry.get(RespondDefaultHandler.class));

        router.get("/id")
                .handler(handlerRegistry.get(AddDefaultHeadersHandler.class))
                .handler(handlerRegistry.get(ReadParametersHandler.class))
                .handler(handlerRegistry.get(ReadSharedIdHandler.class))
                .handler(handlerRegistry.get(SetLocationHandler.class))
                .handler(handlerRegistry.get(ReadAuditCookieHandler.class))
                .handler(handlerRegistry.get(ParseGdprConsentStringHandler.class))
                .handler(handlerRegistry.get(CheckGdprConsentHandler.class))
                .handler(handlerRegistry.get(CheckCcpaConsentHandler.class))
                .handler(handlerRegistry.get(SetSharedIdCookieHandler.class))
                .handler(handlerRegistry.get(RespondSharedIdHandler.class));

        router.post("/id")
                .handler(BodyHandler.create())
                .handler(handlerRegistry.get(AddDefaultHeadersHandler.class))
                .handler(handlerRegistry.get(ReadParametersHandler.class))
                .handler(handlerRegistry.get(ReadSharedIdHandler.class))
                .handler(handlerRegistry.get(SyncSharedIdHandler.class))
                .handler(handlerRegistry.get(SetLocationHandler.class))
                .handler(handlerRegistry.get(ReadAuditCookieHandler.class))
                .handler(handlerRegistry.get(ParseGdprConsentStringHandler.class))
                .handler(handlerRegistry.get(CheckGdprConsentHandler.class))
                .handler(handlerRegistry.get(CheckCcpaConsentHandler.class))
                .handler(handlerRegistry.get(SetSharedIdCookieHandler.class))
                .handler(handlerRegistry.get(RespondSharedIdHandler.class));

        router.get("/pubcid")
                .handler(handlerRegistry.get(PubcidHandler.class));

        router.get("/optout")
                .handler(handlerRegistry.get(AddDefaultHeadersHandler.class))
                .handler(CorsHandler.create(".*.").allowCredentials(true))
                .handler(handlerRegistry.get(ProcessOptOutHandler.class))
                .handler(TemplateHandler.create(templateEngine, optOutTemplate, "text/html"));

        router.get("/optin")
                .handler(handlerRegistry.get(AddDefaultHeadersHandler.class))
                .handler(CorsHandler.create(".*.").allowCredentials(true))
                .handler(handlerRegistry.get(ProcessOptInHandler.class))
                .handler(TemplateHandler.create(templateEngine, optInTemplate, "text/html"));

        router.get("/health")
                .handler(new RespondHealthCheckHandler());

        router.errorHandler(500, routingContext -> {
            logger.error("Failed to handle request", routingContext.failure());
        });

        return router;
    }
}
