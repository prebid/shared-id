package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.util.ExtraHeadersCookie;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class SetSharedIdCookieHandlerTest {
    private SetSharedIdCookieHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Meter meter;

    @Mock
    private RoutingContext routingContext;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        when(routingContext.data()).thenReturn(data);

        handler = new SetSharedIdCookieHandler(
                "sharedid",
                123L,
                true,
                metricRegistry);
    }

    @Test
    public void testIsNewUserId() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setIsNewUserId(true);
        dataContext.setUserId("userid");

        handler.handle(routingContext);

        ArgumentCaptor<ExtraHeadersCookie> captor = ArgumentCaptor.forClass(ExtraHeadersCookie.class);
        verify(routingContext, times(1)).addCookie(captor.capture());

        ExtraHeadersCookie cookie = captor.getValue();

        assertThat(cookie.getName(), is("sharedid"));
        assertThat(cookie.getMaxAge(), is(123L));

        verify(routingContext, times(1)).next();
    }
}
