package org.sharedid.endpoint.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Future;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.model.AuditCookie;
import org.sharedid.endpoint.service.AuditCookieService;
import org.sharedid.endpoint.service.LocationService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class SetAuditCookieHandlerTest {
    private SetAuditCookieHandler handler;

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private Meter meter;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerRequest request;

    @Mock
    private LocationService locationService;

    @Mock
    private AuditCookieService auditCookieService;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(metricRegistry.meter(anyString())).thenReturn(meter);

        when(routingContext.data()).thenReturn(data);

        handler = new SetAuditCookieHandler(
                locationService,
                auditCookieService,
                metricRegistry,
                "audit",
                123L,
                "123",
                true,
                true);
    }

    @Test
    public void testRenewAuditCookie() {
        DataContext dataContext = DataContext.from(routingContext);

        AuditCookie auditCookie = new AuditCookie();
        auditCookie.setRenewedTimestampSeconds(0L);

        dataContext.setAuditCookie(auditCookie);

        handler.handle(routingContext);

        assertThat(auditCookie.getRenewedTimestampSeconds(), is(not(0L)));

        verify(routingContext, times(1)).addCookie(any(Cookie.class));
        verify(routingContext, times(1)).next();
    }

    @Test
    public void testCreateAuditCookie() {
        AuditCookie auditCookie = new AuditCookie();

        when(routingContext.request()).thenReturn(request);
        when(locationService.getCountryForRequest(request)).thenReturn(Future.succeededFuture("US"));
        when(auditCookieService.createAuditCookie(any(), anyString(), anyString(), anyString()))
                .thenReturn(auditCookie);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId("userid");
        dataContext.setGdprConsentParam("consent");

        handler.handle(routingContext);

        verify(routingContext, times(1)).addCookie(any(Cookie.class));
        verify(routingContext, times(1)).next();
    }
}
