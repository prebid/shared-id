package org.sharedid.endpoint.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReadParametersHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(ReadParametersHandler.class);

    public static final String PARAM_IS_GDPR = "gdpr";
    public static final String PARAM_GDPR_CONSENT = "gdpr_consent";
    public static final String PARAM_US_PRIVACY = "us_privacy";
    public static final String PARAM_VENDOR = "vendor";
    public static final String PARAM_REDIRECT_URL = "redir";

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String isGdprParam = routingContext.request().getParam(PARAM_IS_GDPR);
        String gdprConsentParam = routingContext.request().getParam(PARAM_GDPR_CONSENT);
        String usPrivacyParam = routingContext.request().getParam(PARAM_US_PRIVACY);
        String vendorParam = routingContext.request().getParam(PARAM_VENDOR);
        String redirectUrl = routingContext.request().getParam(PARAM_REDIRECT_URL);

        dataContext.setIsGdprParam(isGdprParam);
        dataContext.setGdprConsentParam(gdprConsentParam);
        dataContext.setUsPrivacyParam(usPrivacyParam);

        if (StringUtils.isNotBlank(vendorParam)) {
            try {
                Integer vendor = Integer.valueOf(vendorParam);
                dataContext.setVendor(vendor);
            } catch (NumberFormatException e) {
                logger.debug("Vendor param {} is not a number", vendorParam);
                ResponseUtil.badRequest(routingContext.response());
                return;
            }
        }

        dataContext.setRedirectUrlParam(redirectUrl);

        routingContext.next();
    }
}
