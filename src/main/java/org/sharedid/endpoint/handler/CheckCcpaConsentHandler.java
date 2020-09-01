package org.sharedid.endpoint.handler;

import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.service.LocationService;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CheckCcpaConsentHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(CheckCcpaConsentHandler.class);

    private static final int US_PRIVACY_OPTOUT_INDEX = 2;
    private static final int US_PRIVACY_VERSION_INDEX = 0;
    private static final char US_PRIVACY_VERSION = '1';

    private LocationService locationService;
    private boolean isEnabled;
    private Set<String> ccpaStates;

    @Autowired
    public CheckCcpaConsentHandler(LocationService locationService,
                                   @Value("${handlers.ccpa-consent.enabled}") Boolean isEnabled,
                                   @Value("${handlers.ccpa-consent.states}") Set<String> ccpaStates) {
        this.locationService = locationService;
        this.isEnabled = isEnabled != null ? isEnabled : false;
        this.ccpaStates = ccpaStates;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        if (!isEnabled) {
            routingContext.next();
            return;
        }

        DataContext dataContext = DataContext.from(routingContext);

        String usPrivacyParam = dataContext.getUsPrivacyParam();

        if (usPrivacyParam == null || usPrivacyParam.isEmpty()) {
            logger.debug("us_privacy parameter is not present, consent is implied");
            routingContext.next();
            return;
        }

        if (!isCcpaOptOut(usPrivacyParam)) {
            logger.debug("us_privacy is opted in");
            routingContext.next();
            return;
        }

        //is ccpa opted out, check if request geo state applies

        locationService.getStateForRequest(routingContext.request())
            .map(geoQueryOpt -> geoQueryOpt.orElse("").toLowerCase())      //get state
            .map(state -> ccpaStates.contains(state))
            .setHandler(result -> {
                if (result.failed()) {
                    logger.debug("Failed to get state for request", result.cause());
                    routingContext.response()
                            .setStatusCode(500)
                            .end();
                    return;
                }

                boolean isCcpaState = result.result();

                if (!isCcpaState) {
                    logger.debug("CCPA does not apply");
                    routingContext.next();
                    return;
                }

                logger.debug("CCPA consent is not given");

                routingContext.response()
                        .setStatusCode(204)
                        .end();
            });
    }

    private boolean isCcpaOptOut(String usPrivacy) {
        if (usPrivacy.length() <= US_PRIVACY_OPTOUT_INDEX) {
            logger.debug("us_privacy param does not include opt out, opted in");
            return false;
        }

        if (US_PRIVACY_VERSION != usPrivacy.charAt(US_PRIVACY_VERSION_INDEX)) {
            logger.debug("us_privacy version is not equal to version 1, opted in");
            return false;
        }

        char optOutValue = usPrivacy.charAt(US_PRIVACY_OPTOUT_INDEX);

        logger.debug("us_privacy opt out value {}", optOutValue);

        return 'Y' == Character.toUpperCase(optOutValue);
    }
}
