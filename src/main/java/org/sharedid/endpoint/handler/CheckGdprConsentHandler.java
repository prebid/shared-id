package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerRequest;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.service.LocationService;
import org.sharedid.endpoint.util.GdprUtil;
import io.vertx.core.Future;
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

    public static final int SHARED_ID_VENDOR_ID = 52;

    private static final String METRIC_CONSENT_CHECK_FAILED = "shared-id.handler.consent.consent_check_failed";
    private static final String METRIC_CONSENT_NOT_GIVEN = "shared-id.handler.consent.consent_not_given";
    private static final String METRIC_CONSENT_GIVEN = "shared-id.handler.consent.consent_given";
    private static final String METRIC_CONSENT_GIVEN_BY_CONSENT_STRING =
            "shared-id.handler.consent.consent_given_by_consent_string";
    private static final String METRIC_CONSENT_NOT_GIVEN_BY_GDPR_PARAMETER =
            "shared-id.handler.consent.consent_not_given_by_gdpr_parameter";
    private static final String METRIC_REQUEST_IN_EEA_COUNTRY = "shared-id.handler.consent.request_in_eea_country";
    private static final String METRIC_REQUEST_IN_NON_EEA_COUNTRY = "shared-id.handler.consent.request_in_non_eea_country";
    private static final String METRIC_CONSENT_STRING_FROM_REQUEST_PARAMETER =
            "shared-id.handler.consent.consent_string_from_request_parameter";
    private static final String METRIC_CONSENT_STRING_NOT_FOUND = "shared-id.handler.consent.consent_string_not_found";

    private LocationService locationService;

    private Meter consentCheckFailedMeter;
    private Meter consentNotGivenMeter;
    private Meter consentGivenMeter;
    private Meter consentGivenByConsentStringMeter;
    private Meter consentNotGivenByGdprParameterMeter;
    private Meter requestInEeaCountryMeter;
    private Meter requestInNonEeaCountryMeter;
    private Meter consentStringFromRequestParameterMeter;
    private Meter consentStringNotFoundMeter;

    @Autowired
    public CheckGdprConsentHandler(LocationService locationService, MetricRegistry metricRegistry) {
        this.locationService = locationService;

        this.consentCheckFailedMeter = metricRegistry.meter(METRIC_CONSENT_CHECK_FAILED);
        this.consentNotGivenMeter = metricRegistry.meter(METRIC_CONSENT_NOT_GIVEN);
        this.consentGivenMeter = metricRegistry.meter(METRIC_CONSENT_GIVEN);
        this.consentGivenByConsentStringMeter = metricRegistry.meter(METRIC_CONSENT_GIVEN_BY_CONSENT_STRING);
        this.consentNotGivenByGdprParameterMeter = metricRegistry.meter(METRIC_CONSENT_NOT_GIVEN_BY_GDPR_PARAMETER);
        this.requestInEeaCountryMeter = metricRegistry.meter(METRIC_REQUEST_IN_EEA_COUNTRY);
        this.requestInNonEeaCountryMeter = metricRegistry.meter(METRIC_REQUEST_IN_NON_EEA_COUNTRY);
        this.consentStringFromRequestParameterMeter = metricRegistry.meter(METRIC_CONSENT_STRING_FROM_REQUEST_PARAMETER);
        this.consentStringNotFoundMeter = metricRegistry.meter(METRIC_CONSENT_STRING_NOT_FOUND);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        HttpServerResponse response = routingContext.response();

        hasConsent(dataContext, routingContext.request()).setHandler(consentResult -> {
            if (consentResult.failed()) {
                logger.debug("Failed to check for consent {}", consentResult.cause().getMessage());
                consentCheckFailedMeter.mark();
                response.setStatusCode(204);
                response.end();
                return;
            }

            if (!consentResult.result()) {
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
        });
    }

    private Future<Boolean> hasConsent(DataContext dataContext, HttpServerRequest request) {
        String gdprConsentParam = dataContext.getGdprConsentParam();
        GdprConsentString gdprConsentString = dataContext.getGdprConsentString();

        if (StringUtils.isBlank(gdprConsentParam)) {
            if (hasAuditCookie(dataContext)) {
                //assume that consent was given previously
                return Future.succeededFuture(true);
            }

            consentStringNotFoundMeter.mark();
        } else {
            consentStringFromRequestParameterMeter.mark();
        }

        boolean isConsentGiven = true;

        if (gdprConsentString != null) {
            isConsentGiven = gdprConsentString.isConsentGiven(SHARED_ID_VENDOR_ID);
        } else {
            if (StringUtils.isNotBlank(gdprConsentParam)) {
                //consent string is malformed, consent is not given
                logger.debug("Gdpr consent string malformed");
                isConsentGiven = false;
            } else {
                logger.debug("Gdpr consent string missing");
            }
        }

        if (isConsentGiven) {
            logger.debug("Consent granted");
            consentGivenByConsentStringMeter.mark();
            return Future.succeededFuture(true);
        }

        logger.debug("Consent not given from consent string. Checking if GDPR applies to request.");

        String isGdprParam = dataContext.isGdprParam();

        if ("1".equals(isGdprParam)) {
            //is gdpr param says request is gdpr, so consent not given
            logger.debug("GDPR request parameter is true, so consent not given");
            consentNotGivenByGdprParameterMeter.mark();
            return Future.succeededFuture(false);
        }

        return locationService.getCountryForRequest(request)
            .map(geoQuery -> {
                if (StringUtils.isNotBlank(geoQuery)) {
                    dataContext.setGeoQuery(geoQuery);
                }

                return geoQuery;
            })
            .map(GdprUtil::isGdprRequired)
            .map(isGdprRequired -> {
                //if gdpr is required, consent is not given
                //otherwise, not in an eea country and consent is given
                dataContext.setIsGdprCountry(isGdprRequired);

                if (isGdprRequired) {
                    logger.debug("Request is from an EEA country. Consent is not given");
                    requestInEeaCountryMeter.mark();
                    return false;
                } else {
                    logger.debug("Request is from non-EEA country. Consent is given by default");
                    requestInNonEeaCountryMeter.mark();
                    return true;
                }
            });
    }

    private boolean hasAuditCookie(DataContext dataContext) {
        return dataContext.getAuditCookie() != null;
    }
}
