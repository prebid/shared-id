package org.sharedid.endpoint.handler;

import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;
import org.sharedid.endpoint.model.AuditCookie;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ReadAuditCookieHandlerTest {
    private ReadAuditCookieHandler handler;

    @Mock
    private RoutingContext routingContext;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        handler = new ReadAuditCookieHandler("audit", "123");
    }

    @Test
    public void testNoCookie() {
        when(routingContext.getCookie(anyString())).thenReturn(null);

        handler.handle(routingContext);

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testMalformedCookie() {
        Cookie cookie = Cookie.cookie("audit", "malformed");
        when(routingContext.getCookie(anyString())).thenReturn(cookie);

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        AuditCookie auditCookie = dataContext.getAuditCookie();

        assertThat(auditCookie, is(nullValue()));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testValidCookie() throws Exception {
        AuditCookie auditCookie =
                new AuditCookie(
                        "1",
                        "userid",
                        "1",
                        "1",
                        "country",
                        0L,
                        "referrer",
                        "initiator",
                        "init",
                        true,
                        "consent");

        String serializedValue = auditCookie.serialize("123");

        Cookie cookie = Cookie.cookie("audit", serializedValue);
        when(routingContext.getCookie(anyString())).thenReturn(cookie);

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        AuditCookie savedAuditCookie = dataContext.getAuditCookie();

        assertThat(savedAuditCookie, is(notNullValue()));
        assertThat(savedAuditCookie.getUserId(), is("userid"));

        verify(routingContext, times(1)).next();
    }
}
