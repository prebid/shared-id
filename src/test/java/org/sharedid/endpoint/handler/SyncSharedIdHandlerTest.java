package org.sharedid.endpoint.handler;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class SyncSharedIdHandlerTest {
    private static final String optout = "00000000000000000000000000";

    private SyncSharedIdHandler handler;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        handler = new SyncSharedIdHandler(optout);
    }

    @Test
    public void testMissingBody() {
        when(routingContext.response()).thenReturn(response);

        when(routingContext.getBodyAsJson()).thenReturn(null);

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(400);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }

    @Test
    public void testNonJsonBody() {
        when(routingContext.response()).thenReturn(response);

        when(routingContext.getBodyAsJson()).thenThrow(new DecodeException());

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(400);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }

    @Test
    public void testMissingSharedId() {
        when(routingContext.response()).thenReturn(response);

        when(routingContext.getBodyAsJson()).thenReturn(new JsonObject());

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(400);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }

    @Test
    public void testBlankSharedId() {
        when(routingContext.response()).thenReturn(response);

        JsonObject json = new JsonObject();
        json.put("sharedId", "");
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(400);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }

    @Test
    public void testNonUlid() {
        when(routingContext.response()).thenReturn(response);

        JsonObject json = new JsonObject();
        json.put("sharedId", "nonuuid");
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(400);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }

    @Test
    public void testValidUlid() {
        when(routingContext.data()).thenReturn(data);

        JsonObject json = new JsonObject();
        json.put("sharedId", "01E6HHYAZ4DPSSS0QFQ6GHF9SS");
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);
        String userId = dataContext.getUserId();
        boolean isSyncedUserId = dataContext.isSyncedUserId();

        assertThat(userId, is("01E6HHYAZ4DPSSS0QFQ6GHF9SS"));
        assertThat(isSyncedUserId, is(true));

        verify(response, times(0)).setStatusCode(400);
        verify(response, times(0)).end();
        verify(routingContext, times(1)).next();
    }

    @Test
    public void testOlderCurrentUserId() {
        String olderUlid = "01E6YR5ZPKYP46FJ1JH6PCNT8W";
        String newerUlid = "01E6YR69BDA72B6EQW5Q20C4SD";

        when(routingContext.data()).thenReturn(data);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId(olderUlid);

        JsonObject json = new JsonObject();
        json.put("sharedId", newerUlid);
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        String userId = dataContext.getUserId();
        boolean isSyncedUserId = dataContext.isSyncedUserId();

        assertThat(userId, is(olderUlid));
        assertThat(isSyncedUserId, is(true));

        verify(response, times(0)).setStatusCode(400);
        verify(response, times(0)).end();
        verify(routingContext, times(1)).next();
    }

    @Test
    public void testOlderSyncId() {
        String olderUlid = "01E6YR5ZPKYP46FJ1JH6PCNT8W";
        String newerUlid = "01E6YR69BDA72B6EQW5Q20C4SD";

        when(routingContext.data()).thenReturn(data);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId(newerUlid);

        JsonObject json = new JsonObject();
        json.put("sharedId", olderUlid);
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        String userId = dataContext.getUserId();
        boolean isSyncedUserId = dataContext.isSyncedUserId();

        assertThat(userId, is(olderUlid));
        assertThat(isSyncedUserId, is(true));

        verify(response, times(0)).setStatusCode(400);
        verify(response, times(0)).end();
        verify(routingContext, times(1)).next();
    }

    @Test
    public void testOptedOutSync() {
        String generatedUlid = "01E6YR69BDA72B6EQW5Q20C4SD";

        when(routingContext.data()).thenReturn(data);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId(generatedUlid);

        JsonObject json = new JsonObject();
        json.put("sharedId", optout);
        when(routingContext.getBodyAsJson()).thenReturn(json);

        handler.handle(routingContext);

        String userId = dataContext.getUserId();
        boolean isSyncedUserId = dataContext.isSyncedUserId();

        assertThat(userId, is(generatedUlid));
        assertThat(isSyncedUserId, is(true));

        verify(response, times(0)).setStatusCode(400);
        verify(response, times(0)).end();
        verify(routingContext, times(1)).next();
    }
}
