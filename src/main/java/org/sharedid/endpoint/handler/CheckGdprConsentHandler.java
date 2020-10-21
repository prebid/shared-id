package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerRequest;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.context.DataContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckGdprConsentHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(CheckGdprConsentHandler.class);

    public static final int SHARED_ID_VENDOR_ID = 887;

    private static final String METRIC_CONSENT_NOT_GIVEN = "shared-id.handler.consent.consent_not_given";
    private static final String METRIC_CONSENT_GIVEN = "shared-id.handler.consent.consent_given";
    private static final String METRIC_CONSENT_GIVEN_BY_CONSENT_STRING =
            "shared-id.handler.consent.consent_given_by_consent_string";
    private static final String METRIC_CONSENT_NOT_GIVEN_BY_GDPR_PARAMETER =
            "shared-id.handler.consent.consent_not_given_by_gdpr_parameter";
    private static final String METRIC_CONSENT_STRING_FROM_REQUEST_PARAMETER =
            "shared-id.handler.consent.consent_string_from_request_parameter";
    private static final String METRIC_CONSENT_STRING_NOT_FOUND = "shared-id.handler.consent.consent_string_not_found";

    private Meter consentNotGivenMeter;
    private Meter consentGivenMeter;
    private Meter consentGivenByConsentStringMeter;
    private Meter consentNotGivenByGdprParameterMeter;
    private Meter consentStringFromRequestParameterMeter;
    private Meter consentStringNotFoundMeter;

    @Autowired
    public CheckGdprConsentHandler(MetricRegistry metricRegistry) {
        this.consentNotGivenMeter = metricRegistry.meter(METRIC_CONSENT_NOT_GIVEN);
        this.consentGivenMeter = metricRegistry.meter(METRIC_CONSENT_GIVEN);
        this.consentGivenByConsentStringMeter = metricRegistry.meter(METRIC_CONSENT_GIVEN_BY_CONSENT_STRING);
        this.consentNotGivenByGdprParameterMeter = metricRegistry.meter(METRIC_CONSENT_NOT_GIVEN_BY_GDPR_PARAMETER);
        this.consentStringFromRequestParameterMeter = metricRegistry.meter(METRIC_CONSENT_STRING_FROM_REQUEST_PARAMETER);
        this.consentStringNotFoundMeter = metricRegistry.meter(METRIC_CONSENT_STRING_NOT_FOUND);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        HttpServerResponse response = routingContext.response();

        boolean hasConsent = getConsent(dataContext, routingContext.request());

        if (!hasConsent) {
            logger.debug("Consent not given");
            consentNotGivenMeter.mark();

            //does not have consent
            //do not redirect
            //do not write cookies

            response.setStatusCode(204);
            response.end();
            return;
        }

        logger.debug("Consent given");
        consentGivenMeter.mark();

        routingContext.next();
    }

    private Boolean getConsent(DataContext dataContext, HttpServerRequest request) {
        String gdprConsentParam = dataContext.getGdprConsentParam();
        GdprConsentString gdprConsentString = dataContext.getGdprConsentString();

        if (StringUtils.isBlank(gdprConsentParam)) {
            if (hasAuditCookie(dataContext)) {
                //assume that consent was given previously
                return true;
            }

            consentStringNotFoundMeter.mark();
        } else {
            consentStringFromRequestParameterMeter.mark();
        }

        boolean isConsentGiven = false;

        if (gdprConsentString != null) {
            isConsentGiven = gdprConsentString.isConsentGiven(SHARED_ID_VENDOR_ID);
        } else {
            if (StringUtils.isNotBlank(gdprConsentParam)) {
                //consent string is malformed, consent is not given
                logger.debug("Gdpr consent string malformed");
            } else {
                logger.debug("Gdpr consent string missing");
            }
        }

        if (isConsentGiven) {
            logger.debug("Consent granted");
            consentGivenByConsentStringMeter.mark();
            return true;
        }

        logger.debug("Consent not given from consent string. Checking if GDPR applies to request.");

        String isGdprParam = dataContext.isGdprParam();

        if ("1".equals(isGdprParam)) {
            //is gdpr param says request is gdpr, so consent not given
            logger.debug("GDPR request parameter is true, so consent not given");
            consentNotGivenByGdprParameterMeter.mark();
            return false;
        }

        boolean isGdprCountry = dataContext.getIsGdprCountry();

        return !isGdprCountry;
    }

    private boolean hasAuditCookie(DataContext dataContext) {
        return dataContext.getAuditCookie() != null;
    }
}
