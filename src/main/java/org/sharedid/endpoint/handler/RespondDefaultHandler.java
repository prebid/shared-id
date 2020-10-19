package org.sharedid.endpoint.handler;

import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ResponseUtil;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RespondDefaultHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(RespondDefaultHandler.class);

    @Override
    public void handle(RoutingContext routingContext) {
        DataContext dataContext = DataContext.from(routingContext);

        String userId = dataContext.getUserId();

        //TODO: before turning this over to prebid.org, remove the default response to rubicon
        String redirectUrl = "https://pixel.rubiconproject.com/tap.php?v=624210&nid=2231&put=" + userId;

        logger.debug("Sending default redirect to {}", redirectUrl);

        ResponseUtil.redirect(routingContext.response(), redirectUrl);
    }
}
