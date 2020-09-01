package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.context.DataContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class RedirectHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(RedirectHandler.class);

    private static final String METRIC_REDIRECT_URL_NOT_PRESENT = "shared-id.handler.redirect.redirect_url_not_present";
    private static final String METRIC_REDIRECTING = "shared-id.handler.redirect.redirect";
    private static final String METRIC_REDIRECT_URL_MALFORMED = "shared-id.handler.redirect.redirect_url_malformed";
    private static final String METRIC_GDPR_PARAMETER_ADDED_TO_REDIRECT =
            "shared-id.handler.redirect.gdpr_parameter_added_to_redirect";
    private static final String METRIC_CONSENT_STRING_ADDED_TO_REDIRECT =
            "shared-id.handler.redirect.consent_string_added_to_redirect";

    private static final String REDIRECT_PARAMETER_GDPR = "gdpr";
    private static final String REDIRECT_PARAMETER_GDPR_CONSENT = "gdpr_consent";

    private static final String MACRO_USER_ID = "{user_token}";

    private Meter redirectUrlNotPresentMeter;
    private Meter redirectingMeter;
    private Meter redirectUrlMalformedMeter;
    private Meter gdprParameterAddedToRedirectMeter;
    private Meter consentStringAddedToRedirectMeter;

    @Autowired
    public RedirectHandler(MetricRegistry metricRegistry) {
        this.redirectUrlNotPresentMeter = metricRegistry.meter(METRIC_REDIRECT_URL_NOT_PRESENT);
        this.redirectingMeter = metricRegistry.meter(METRIC_REDIRECTING);
        this.redirectUrlMalformedMeter = metricRegistry.meter(METRIC_REDIRECT_URL_MALFORMED);
        this.gdprParameterAddedToRedirectMeter = metricRegistry.meter(METRIC_GDPR_PARAMETER_ADDED_TO_REDIRECT);
        this.consentStringAddedToRedirectMeter = metricRegistry.meter(METRIC_CONSENT_STRING_ADDED_TO_REDIRECT);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String redirectUrl = dataContext.getRedirectUrlParam();

        if (redirectUrl == null) {
            logger.debug("Redirect url parameter not present");
            redirectUrlNotPresentMeter.mark();
            routingContext.next();
            return;
        }

        try {
            redirectUrl = replaceMacros(dataContext, redirectUrl);
            URIBuilder uriBuilder = new URIBuilder(redirectUrl);

            uriBuilder = addGdprParameters(dataContext, uriBuilder);

            HttpServerResponse response = routingContext.response();

            String finalRedirectUrl = uriBuilder.build().toString();

            logger.debug("Redirecting to {}", finalRedirectUrl);
            redirectingMeter.mark();

            response.setStatusCode(302);
            response.putHeader("Location", finalRedirectUrl);
            response.end();
        } catch (URISyntaxException e) {
            logger.debug("Received invalid redirect url parameter {}", redirectUrl);
            redirectUrlMalformedMeter.mark();
            routingContext.next();
        }
    }

    private String replaceMacros(DataContext dataContext, String redirectUrl) {
        String userId = dataContext.getUserId();

        redirectUrl = redirectUrl.replace(MACRO_USER_ID, userId);

        return redirectUrl;
    }

    private URIBuilder addGdprParameters(DataContext dataContext, URIBuilder uriBuilder) {
        String isGdprParam = dataContext.isGdprParam();

        if ("1".equals(isGdprParam)) {
            logger.debug("Adding GDPR parameter to redirect url");
            gdprParameterAddedToRedirectMeter.mark();
            uriBuilder = uriBuilder.addParameter(REDIRECT_PARAMETER_GDPR, "1");
        }

        String gdprConsentParam = dataContext.getGdprConsentParam();

        if (StringUtils.isNotEmpty(gdprConsentParam)) {
            consentStringAddedToRedirectMeter.mark();
            uriBuilder = uriBuilder.addParameter(REDIRECT_PARAMETER_GDPR_CONSENT, gdprConsentParam);
        }

        return uriBuilder;
    }
}
