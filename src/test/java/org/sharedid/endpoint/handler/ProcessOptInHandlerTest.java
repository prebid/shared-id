package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ProcessOptInHandlerTest {
    private ProcessOptInHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Meter meter;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerRequest request;

    @Mock
    private HttpServerResponse response;

    @Mock
    private Cookie cookie;

    private final String sharedIdCookieName = "cookieName";

    @BeforeEach
    public void setup() {
        when(routingContext.request()).thenReturn(request);
        when(routingContext.response()).thenReturn(response);

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        handler = new ProcessOptInHandler(sharedIdCookieName, metricRegistry);
    }

    @Test
    public void testNoOptOutCookieNoRedirect() {
        when(routingContext.getCookie(sharedIdCookieName)).thenReturn(null);
        when(request.getParam(anyString())).thenReturn(null);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testOptOutCookieNoRedirect() {
        when(request.getParam(anyString())).thenReturn(null);
        when(routingContext.getCookie(anyString())).thenReturn(cookie);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
        verify(cookie, times(1)).setMaxAge(0L);
        verify(response, times(1)).addCookie(cookie);
    }

    @Test
    public void testOptOutCookieWithRedirect() {
        when(request.getParam(anyString())).thenReturn("redirect");
        when(routingContext.getCookie(anyString())).thenReturn(cookie);

        handler.handle(routingContext);

        verify(routingContext, times(0)).next();
        verify(response, times(1)).setStatusCode(302);
        verify(response, times(1)).putHeader("Location", "redirect");
        verify(response, times(1)).end();
        verify(cookie, times(1)).setMaxAge(0L);
        verify(response, times(1)).addCookie(cookie);
    }
}
