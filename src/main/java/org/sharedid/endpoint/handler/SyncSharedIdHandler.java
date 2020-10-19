package org.sharedid.endpoint.handler;

import de.huxhorn.sulky.ulid.ULID;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SyncSharedIdHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SyncSharedIdHandler.class);

    private String sharedIdOptOutValue;

    @Autowired
    public SyncSharedIdHandler(@Value("${cookie.shared-id.opt-out-value}") String sharedIdOptOutValue) {
        this.sharedIdOptOutValue = sharedIdOptOutValue;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        JsonObject json;

        try {
            json = routingContext.getBodyAsJson();
        } catch (DecodeException e) {
            logger.debug("Body is not json");
            ResponseUtil.badRequest(routingContext.response());
            return;
        }

        if (json == null) {
            logger.debug("Body is missing");
            ResponseUtil.badRequest(routingContext.response());
            return;
        }

        String newSharedId = json.getString("sharedId");

        if (StringUtils.isBlank(newSharedId)) {
            logger.debug("Blank shared id");
            ResponseUtil.badRequest(routingContext.response());
            return;
        }

        DataContext dataContext = DataContext.from(routingContext);

        //validate that new shared id is a valid ulid

        long newSharedIdTimestamp;

        try {
            newSharedIdTimestamp = ULID.parseULID(newSharedId).timestamp();
        } catch (IllegalArgumentException e) {
            logger.debug("Body parameter is not a valid ULID {}", newSharedId, e);
            ResponseUtil.badRequest(routingContext.response());
            return;
        }

        String userId = dataContext.getUserId();

        if (StringUtils.isBlank(userId)) {
            dataContext.setUserId(newSharedId);
            dataContext.setIsSyncedUserId(true);
            routingContext.next();
            return;
        }

        if (newSharedId.equals(sharedIdOptOutValue) || userId.equals(sharedIdOptOutValue)) {
            dataContext.setUserId(userId);
            dataContext.setIsSyncedUserId(true);
            routingContext.next();
            return;
        }

        //since older id's are more valuable than newer id's
        //when choosing which id to use, prefer the older

        long cookieIdTimestamp = Long.MAX_VALUE;

        try {
            cookieIdTimestamp = ULID.parseULID(userId).timestamp();
        } catch (IllegalArgumentException e) {
            logger.debug("Current id is not a valid ULID, using new id");
        }

        if (newSharedIdTimestamp < cookieIdTimestamp) { //new shared id is older
            userId = newSharedId;
        }   //otherwise use the current shared id

        dataContext.setUserId(userId);
        dataContext.setIsSyncedUserId(true);
        routingContext.next();
    }
}
