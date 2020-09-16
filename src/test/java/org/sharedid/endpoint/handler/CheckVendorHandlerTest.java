package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.service.LocationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        VertxExtension.class,
        MockitoExtension.class
})
public class CheckVendorHandlerTest {
    private CheckVendorHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Meter meter;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    @Mock
    private LocationService locationService;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        handler = new CheckVendorHandler(locationService, metricRegistry);
    }

    @Test
    public void testNoVendorId() {
        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testNoGdprConsentString() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setVendor(100);
        dataContext.setIsGdprCountry(true);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testIsConsentGiven() throws Exception {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setVendor(52);
        dataContext.setGdprConsentString(new GdprConsentString("COuQACgOuQACgM-AAAENAPCAAIAAAIAAAAAAAjAAAAAAAABAAAAEYAAAAAAAAIAAAAA="));
        dataContext.setIsGdprCountry(true);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testIsConsentDenied() throws Exception {
        when(routingContext.response()).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setVendor(52);
        dataContext.setGdprConsentString(new GdprConsentString("COuQACgOuQACgM-AAAENAPCAAIAAAIAAAAAAAjAAAAAAQIAAAAAAAAAAAA=="));
        dataContext.setIsGdprCountry(true);

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(204);
        verify(response, times(1)).end();
        verify(routingContext, times(0)).next();
    }
}
