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
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.model.AuditCookie;
import org.sharedid.endpoint.service.LocationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        VertxExtension.class,
        MockitoExtension.class
})
public class CheckGdprConsentHandlerTest {
    private CheckGdprConsentHandler handler;

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

        handler = new CheckGdprConsentHandler(metricRegistry, 887);
    }

    @Test
    public void testMissingGdprConsentParamAndHasAuditCookie() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setAuditCookie(new AuditCookie());

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testMissingGdprConsentParam() {
        when(routingContext.response()).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setIsGdprCountry(false);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
        verify(response, times(0)).end();
    }

    @Test
    public void testMalformedGdprString(Vertx vertx, VertxTestContext context) {
        when(routingContext.response()).thenReturn(response);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setIsGdprCountry(true);

        dataContext.setGdprConsentParam("malformed");
        dataContext.setGdprConsentString(null);

        doAnswer(invocation -> {
            verify(routingContext, times(0)).next();
            verify(response, times(1)).setStatusCode(204);
            verify(response, times(1)).end();
            context.completeNow();
            return null;
        }).when(response).end();

        handler.handle(routingContext);
    }

    @Test
    public void testGdprConsentStringGranted() throws Exception {
        when(routingContext.response()).thenReturn(response);

        String consentString = "CPC1ggVPC1ggVM-AAAENBQCAAIAAAAAAAAAAG7wAQG7gAAAA";

        GdprConsentString gdprConsentString =
                new GdprConsentString(consentString);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setGdprConsentParam(consentString);
        dataContext.setGdprConsentString(gdprConsentString);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testGdprConsentStringDeniedEEACountry(Vertx vertx, VertxTestContext context) throws Exception {
        when(routingContext.response()).thenReturn(response);

        String consentString = "COuQACgOuQACgM-AAAENAPCAAIAAAIAAAAAAAjQAYAFABQAAAAAA";

        GdprConsentString gdprConsentString =
                new GdprConsentString(consentString);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setIsGdprCountry(true);
        dataContext.setGdprConsentParam(consentString);
        dataContext.setGdprConsentString(gdprConsentString);

        doAnswer(invocation -> {
            verify(routingContext, times(0)).next();
            verify(response, times(1)).setStatusCode(204);
            verify(response, times(1)).end();
            context.completeNow();
            return null;
        }).when(response).end();

        handler.handle(routingContext);
    }

    @Test
    public void testGdprConsentStringDeniedNonEEACountry(Vertx vertx, VertxTestContext context) throws Exception {
        when(routingContext.response()).thenReturn(response);

        String consentString = "COuQACgOuQACgM-AAAENAPCAAIAAAIAAAAAAAjQAYAFABQAAAAAA";

        GdprConsentString gdprConsentString =
                new GdprConsentString(consentString);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setIsGdprCountry(false);
        dataContext.setGdprConsentParam(consentString);
        dataContext.setGdprConsentString(gdprConsentString);

        doAnswer(invocation -> {
            verify(routingContext, times(1)).next();
            verify(response, times(0)).setStatusCode(204);
            verify(response, times(0)).end();
            context.completeNow();
            return null;
        }).when(routingContext).next();

        handler.handle(routingContext);
    }
}
