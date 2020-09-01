package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class RedirectHandlerTest {
    private RedirectHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Meter meter;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        when(routingContext.data()).thenReturn(data);

        handler = new RedirectHandler(metricRegistry);
    }

    @Test
    public void testMissingRedirectUrl() {
        handler.handle(routingContext);
        verify(routingContext, times(1)).next();
    }

    @Test
    public void testRedirect() {
        when(routingContext.response()).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setRedirectUrlParam("http://test.com/sync?userid={user_token}");
        dataContext.setUserId("userid");
        dataContext.setIsGdprParam("1");
        dataContext.setGdprConsentParam("consent");

        handler.handle(routingContext);

        verify(response, times(1)).setStatusCode(302);
        verify(response, times(1))
                .putHeader("Location", "http://test.com/sync?userid=userid&gdpr=1&gdpr_consent=consent");
        verify(routingContext, times(0)).next();
    }
}
