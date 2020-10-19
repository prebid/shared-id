package org.sharedid.endpoint.handler;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class AddRequestMetricsHandlerTest {
    private AddRequestMetricsHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Timer timer;

    @Mock
    private Timer.Context timerContext;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    @Mock
    private HttpServerRequest request;

    @BeforeEach
    public void setup() {
        when(timer.time()).thenReturn(timerContext);
        when(metricRegistry.timer(anyString())).thenReturn(timer);

        when(routingContext.response()).thenReturn(response);
        when(routingContext.request()).thenReturn(request);

        handler = new AddRequestMetricsHandler(metricRegistry);
    }

    @Test
    public void testAddMetrics() {
        when(request.path()).thenReturn("/test");
        when(request.uri()).thenReturn("/test");

        handler.handle(routingContext);

        ArgumentCaptor<Handler<Void>> captor = ArgumentCaptor.forClass(Handler.class);
        verify(response, times(1)).bodyEndHandler(captor.capture());

        Handler<Void> bodyEndHandler = captor.getValue();

        bodyEndHandler.handle(null);

        verify(timerContext, times(2)).stop();

        verify(routingContext, times(1)).next();
    }
}
