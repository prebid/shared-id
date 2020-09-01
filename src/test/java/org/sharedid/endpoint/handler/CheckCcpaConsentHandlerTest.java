package org.sharedid.endpoint.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.service.LocationService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith({
        VertxExtension.class,
        MockitoExtension.class
})
public class CheckCcpaConsentHandlerTest {
    private CheckCcpaConsentHandler handler;

    @Mock
    private LocationService locationService;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerRequest request;

    @Mock
    private HttpServerResponse response;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        handler = new CheckCcpaConsentHandler(locationService, true, Collections.singleton("ca"));
    }

    @Test
    public void testMissingUsPrivacyParam() {
        DataContext dataContext = DataContext.from(routingContext);

        dataContext.setUsPrivacyParam(null);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(routingContext, times(0)).response();
    }

    @Test
    public void testUsPrivacyParamConsentGranted() {
        DataContext dataContext = DataContext.from(routingContext);

        dataContext.setUsPrivacyParam("1NN");

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(routingContext, times(0)).response();
    }

    @Test
    public void testUsPrivacyParamConsentDeniedNotInCa(Vertx vertx, VertxTestContext context) {
        when(routingContext.request()).thenReturn(request);
        when(locationService.getStateForRequest(any(HttpServerRequest.class)))
                .thenReturn(Future.succeededFuture(Optional.of("wy")));

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUsPrivacyParam("1YY");

        doAnswer(invocation -> {
            context.completeNow();
            return null;
        }).when(routingContext).next();

        handler.handle(routingContext);
    }

    @Test
    public void testUsPrivacyParamConsentDeniedInCa(Vertx vertx, VertxTestContext context) {
        when(routingContext.request()).thenReturn(request);
        when(locationService.getStateForRequest(any(HttpServerRequest.class)))
                .thenReturn(Future.succeededFuture(Optional.of("ca")));
        when(routingContext.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUsPrivacyParam("1YY");

        doAnswer(invocation -> {
            verify(response, times(1)).setStatusCode(204);
            context.completeNow();
            return null;
        }).when(response).end();

        handler.handle(routingContext);
    }

    @Test
    public void testUsPrivacyParamConsentDeniedFailedLocation(Vertx vertx, VertxTestContext context) {
        when(routingContext.request()).thenReturn(request);
        when(locationService.getStateForRequest(any(HttpServerRequest.class)))
                .thenReturn(Future.failedFuture("Failed to get location"));
        when(routingContext.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUsPrivacyParam("1YY");

        doAnswer(invocation -> {
            verify(response, times(1)).setStatusCode(500);
            context.completeNow();
            return null;
        }).when(response).end();

        handler.handle(routingContext);
    }
}
