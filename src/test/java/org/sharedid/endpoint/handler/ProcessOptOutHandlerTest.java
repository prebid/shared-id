package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.util.ExtraHeadersCookie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ProcessOptOutHandlerTest {
    private ProcessOptOutHandler handler;

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

    @BeforeEach
    public void setup() {
        when(routingContext.request()).thenReturn(request);
        when(routingContext.response()).thenReturn(response);

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        handler = new ProcessOptOutHandler(
                "sharedid",
                "audit",
                "00",
                123L,
                true,
                metricRegistry);
    }

    @Test
    public void testNoRedirect() {
        handler.handle(routingContext);

        ArgumentCaptor<ExtraHeadersCookie> cookieArgumentCaptor = ArgumentCaptor.forClass(ExtraHeadersCookie.class);

        verify(response, times(2)).addCookie(cookieArgumentCaptor.capture());

        ExtraHeadersCookie auditCookie = cookieArgumentCaptor.getAllValues().get(0);
        ExtraHeadersCookie sharedIdCookie = cookieArgumentCaptor.getAllValues().get(1);

        assertThat(auditCookie.getName(), is("audit"));
        assertThat(auditCookie.getValue(), is(""));
        assertThat(auditCookie.getMaxAge(), is(0L));
        assertThat(sharedIdCookie.getName(), is("sharedid"));
        assertThat(sharedIdCookie.getValue(), is("00"));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testRedirect() {
        when(request.getParam(anyString())).thenReturn("redirect");

        handler.handle(routingContext);

        ArgumentCaptor<ExtraHeadersCookie> cookieArgumentCaptor = ArgumentCaptor.forClass(ExtraHeadersCookie.class);

        verify(response, times(2)).addCookie(cookieArgumentCaptor.capture());

        ExtraHeadersCookie auditCookie = cookieArgumentCaptor.getAllValues().get(0);
        ExtraHeadersCookie sharedIdCookie = cookieArgumentCaptor.getAllValues().get(1);

        assertThat(auditCookie.getName(), is("audit"));
        assertThat(auditCookie.getValue(), is(""));
        assertThat(auditCookie.getMaxAge(), is(0L));
        assertThat(sharedIdCookie.getName(), is("sharedid"));
        assertThat(sharedIdCookie.getValue(), is("00"));

        verify(routingContext, times(0)).next();
        verify(response, times(1)).setStatusCode(302);
        verify(response, times(1)).putHeader("Location", "redirect");
        verify(response, times(1)).end();
    }
}
