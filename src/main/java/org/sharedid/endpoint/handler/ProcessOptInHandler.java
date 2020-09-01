package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.util.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessOptInHandler implements Handler<RoutingContext> {
    public static final String PARAM_REDIRECT = "redir";

    public static final String METRIC_OPT_IN_REDIRECT_PARAMETER_EXISTS =
            "shared-id.handler.optin.redirect_parameter_exists";

    private String sharedIdCookieName;

    private Meter optInRedirectParameterExists;

    @Autowired
    public ProcessOptInHandler(@Value("${cookie.shared-id.name}") String sharedIdCookieName,
                               MetricRegistry metricRegistry) {
        this.sharedIdCookieName = sharedIdCookieName;

        this.optInRedirectParameterExists = metricRegistry.meter(METRIC_OPT_IN_REDIRECT_PARAMETER_EXISTS);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

        Cookie optOutCookie = routingContext.getCookie(sharedIdCookieName);
        if (optOutCookie != null) {
            optOutCookie.setMaxAge(0);
            response.addCookie(optOutCookie);
        }

        String redirectParam = routingContext.request().getParam(PARAM_REDIRECT);

        if (StringUtils.isNotBlank(redirectParam)) {
            optInRedirectParameterExists.mark();
            ResponseUtil.redirect(response, redirectParam);
            return;
        }

        routingContext.next();
    }
}
