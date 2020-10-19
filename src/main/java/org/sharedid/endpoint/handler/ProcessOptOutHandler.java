package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.util.ExtraHeadersCookie;
import org.sharedid.endpoint.util.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessOptOutHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessOptOutHandler.class);

    public static final String METRIC_OPT_OUT_REDIRECT_PARAMETER_EXISTS =
            "shared-id.handler.optout.redirect_parameter_exists";

    public static final String PARAM_REDIRECT = "redir";
    
    private String sharedIdCookieName;
    private String auditCookieName;
    private String sharedIdOptOutValue;
    private Long sharedIdOptOutTtl;
    private Boolean isSecureCookiesEnabled;

    private Meter optOutRedirectParameterExists;

    @Autowired
    public ProcessOptOutHandler(@Value("${cookie.shared-id.name}") String sharedIdCookieName,
                                @Value("${cookie.audit.name}") String auditCookieName,
                                @Value("${cookie.shared-id.opt-out-value}") String sharedIdOptOutValue,
                                @Value("${cookie.shared-id.opt-out-ttl}") Long sharedIdOptOutTtl,
                                @Value("${cookies.secure}") Boolean isSecureCookiesEnabled,
                                MetricRegistry metricRegistry) {
        this.sharedIdCookieName = sharedIdCookieName;
        this.auditCookieName = auditCookieName;
        this.sharedIdOptOutValue = sharedIdOptOutValue;
        this.sharedIdOptOutTtl = sharedIdOptOutTtl;
        this.isSecureCookiesEnabled = isSecureCookiesEnabled;

        this.optOutRedirectParameterExists = metricRegistry.meter(METRIC_OPT_OUT_REDIRECT_PARAMETER_EXISTS);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        logger.debug("Processing opt out request");

        HttpServerResponse response = routingContext.response();

        boolean isSecure = isSecureCookiesEnabled;

        //remove audit cookie
        Cookie auditCookie =
                ExtraHeadersCookie.fromCookie(Cookie.cookie(auditCookieName, ""));
        auditCookie.setMaxAge(0);
        auditCookie.setSecure(isSecure);
        response.addCookie(auditCookie);

        //set shared id cookie to opted out value
        Cookie optedOutCookie =
                ExtraHeadersCookie.fromCookie(Cookie.cookie(sharedIdCookieName, sharedIdOptOutValue));
        optedOutCookie.setMaxAge(sharedIdOptOutTtl);
        optedOutCookie.setSecure(isSecure);
        response.addCookie(optedOutCookie);

        String redirectParam = routingContext.request().getParam(PARAM_REDIRECT);

        if (StringUtils.isNotBlank(redirectParam)) {
            optOutRedirectParameterExists.mark();
            ResponseUtil.redirect(response, redirectParam);
            return;
        }

        routingContext.put("shared_id_cookie_name", sharedIdCookieName);
        routingContext.put("opt_out_value", sharedIdOptOutValue);

        routingContext.next();
    }
}
