package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        VertxExtension.class,
        MockitoExtension.class
})
public class SetLocationHandlerTest {
    private SetLocationHandler handler;

    @Mock
    private LocationService locationService;

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

        when(routingContext.data()).thenReturn(data);

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        handler = new SetLocationHandler(locationService, metricRegistry);
    }

    @Test
    public void testLocationIsSet(Vertx vertx, VertxTestContext context) {
        when(locationService.getCountryForRequest(any()))
                .thenReturn(Future.succeededFuture("uk"));

        DataContext dataContext = DataContext.from(routingContext);

        doAnswer(invocation -> {
            context.verify(() -> assertThat(dataContext.getGeoQuery(), is("uk")));
            context.verify(() -> assertThat(dataContext.getIsGdprCountry(), is(true)));
            verify(response, times(0)).end();
            context.completeNow();
            return null;
        }).when(routingContext).next();

        handler.handle(routingContext);
    }
}
