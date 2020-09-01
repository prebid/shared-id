package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CheckVendorHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(CheckVendorHandler.class);

    private static final String METRIC_VENDOR_PARAMETER_NOT_PRESENT =
            "shared-id.handler.vendor_check.vendor_parameter_not_present";
    private static final String METRIC_CONSENT_STRING_NOT_PRESENT =
            "shared-id.handler.vendor_check.consent_string_not_present";
    private static final String METRIC_VENDOR_HAS_CONSENT =
            "shared-id.handler.vendor_check.vendor_has_consent";
    private static final String METRIC_VENDOR_NO_CONSENT =
            "shared-id.handler.vendor_check.vendor_no_consent";

    private Meter vendorParameterNotPresentMeter;
    private Meter consentStringNotPresentMeter;
    private Meter vendorHasConsentMeter;
    private Meter vendorNoConsentMeter;

    @Autowired
    public CheckVendorHandler(MetricRegistry metricRegistry) {
        this.vendorParameterNotPresentMeter = metricRegistry.meter(METRIC_VENDOR_PARAMETER_NOT_PRESENT);
        this.consentStringNotPresentMeter = metricRegistry.meter(METRIC_CONSENT_STRING_NOT_PRESENT);
        this.vendorHasConsentMeter = metricRegistry.meter(METRIC_VENDOR_HAS_CONSENT);
        this.vendorNoConsentMeter = metricRegistry.meter(METRIC_VENDOR_NO_CONSENT);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        Integer vendor = dataContext.getVendor();

        if (vendor == null) {
            logger.debug("Vendor parameter not present");
            vendorParameterNotPresentMeter.mark();
            routingContext.next();
            return;
        }

        //if we get here, shared id has consent, but we need to check specifically for vendor

        GdprConsentString gdprConsentString = dataContext.getGdprConsentString();

        if (gdprConsentString == null) {
            logger.debug("Consent string not present");
            consentStringNotPresentMeter.mark();
            routingContext.next();
            return;
        }

        boolean isConsentGiven = gdprConsentString.isConsentGiven(vendor);

        if (isConsentGiven) {
            logger.debug("Vendor has consent");
            vendorHasConsentMeter.mark();
            routingContext.next();
            return;
        }

        logger.debug(
                "Consent not provided for vendor {} in consent string {}",
                vendor,
                gdprConsentString.getRawConsentString());
        vendorNoConsentMeter.mark();

        //end response early
        ResponseUtil.noContent(routingContext.response());
    }
}
