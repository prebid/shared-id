package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.service.LocationService;
import org.sharedid.endpoint.util.GdprUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetLocationHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SetLocationHandler.class);

    private static final String METRIC_REQUEST_IN_EEA_COUNTRY = "shared-id.handler.location.request_in_eea_country";
    private static final String METRIC_REQUEST_IN_NON_EEA_COUNTRY = "shared-id.handler.location.request_in_non_eea_country";
    private static final String METRIC_LOCATION_REQUEST_FAILED = "shared-id.handler.location.failed";

    private LocationService locationService;

    private Meter requestInEeaCountryMeter;
    private Meter requestInNonEeaCountryMeter;
    private Meter locationRequestFailed;

    @Autowired
    public SetLocationHandler(LocationService locationService,
                              MetricRegistry metricRegistry) {
        this.locationService = locationService;

        this.requestInEeaCountryMeter = metricRegistry.meter(METRIC_REQUEST_IN_EEA_COUNTRY);
        this.requestInNonEeaCountryMeter = metricRegistry.meter(METRIC_REQUEST_IN_NON_EEA_COUNTRY);
        this.locationRequestFailed = metricRegistry.meter(METRIC_LOCATION_REQUEST_FAILED);
    }

    @Override
    public void handle(RoutingContext context) {
        DataContext dataContext = DataContext.from(context);

        HttpServerResponse response = context.response();

        locationService.getCountryForRequest(context.request())
                .map(geoQuery -> {
                    if (StringUtils.isNotBlank(geoQuery)) {
                        dataContext.setGeoQuery(geoQuery);
                    }

                    return geoQuery;
                })
                .map(GdprUtil::isGdprRequired)
                .onComplete(result -> {
                    if (result.failed()) {
                        logger.debug("Failed to read country for request", result.cause());

                        locationRequestFailed.mark();

                        response.setStatusCode(204);
                        response.end();
                        return;
                    }

                    boolean isGdprRequired = result.result();

                    //if gdpr is required, consent is not given
                    //otherwise, not in an eea country and consent is given
                    dataContext.setIsGdprCountry(isGdprRequired);

                    if (isGdprRequired) {
                        logger.debug("Request is from an EEA country. Consent is not given");
                        requestInEeaCountryMeter.mark();
                    } else {
                        logger.debug("Request is from non-EEA country. Consent is given by default");
                        requestInNonEeaCountryMeter.mark();
                    }

                    context.next();
                });
    }
}
