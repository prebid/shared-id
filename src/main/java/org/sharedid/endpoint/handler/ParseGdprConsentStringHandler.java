package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.consent.GdprConsentStringException;
import org.sharedid.endpoint.context.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParseGdprConsentStringHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(ParseGdprConsentStringHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String gdprConsentStringParam = dataContext.getGdprConsentParam();

        if (StringUtils.isBlank(gdprConsentStringParam)) {
            routingContext.next();
            return;
        }

        try {
            GdprConsentString gdprConsentString = new GdprConsentString(gdprConsentStringParam);
            dataContext.setGdprConsentString(gdprConsentString);
        } catch (GdprConsentStringException e) {
            logger.debug("Failed to parse consent string", e);
        }

        routingContext.next();
    }
}
