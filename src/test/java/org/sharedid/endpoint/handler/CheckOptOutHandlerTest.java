package org.sharedid.endpoint.handler;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith({
        VertxExtension.class,
        MockitoExtension.class
})
public class CheckOptOutHandlerTest {
    private CheckOptOutHandler handler;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        handler = new CheckOptOutHandler(
                "00",
                "sharedid",
                123L,
                false,
                true);
    }

    @Test
    public void testNoUserId() {
        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testOptedInUserId() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId("opted-in");

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testOptedOutUserId() {
        when(routingContext.response()).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId("00");

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(204);
        verify(response, times(1)).end();
        verify(response, times(1)).addCookie(any(Cookie.class));
        verify(routingContext, times(0)).next();
    }
}
