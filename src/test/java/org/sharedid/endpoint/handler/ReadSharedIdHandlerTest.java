package org.sharedid.endpoint.handler;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith({
        MockitoExtension.class
})
public class ReadSharedIdHandlerTest {
    private ReadSharedIdHandler handler;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerRequest request;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);
        when(routingContext.request()).thenReturn(request);

        handler = new ReadSharedIdHandler("sharedid", "myads");
    }

    @Test
    public void testNoCookies() {
        when(request.getCookie(anyString())).thenReturn(null);

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        assertThat(dataContext.getUserId(), is(notNullValue()));
        assertThat(dataContext.isNewUserId(), is(true));
    }

    @Test
    public void testSharedIdCookie() {
        when(request.getCookie(anyString()))
                .thenReturn(Cookie.cookie("sharedid", "01E6F7X375B2FEWHD3HMMFST6F"));

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        assertThat(dataContext.getUserId(), is("01E6F7X375B2FEWHD3HMMFST6F"));
        assertThat(dataContext.isNewUserId(), is(false));
    }

    @Test
    public void testLegacyCookie() {
        String uuid = UUID.randomUUID().toString();

        when(request.getCookie(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0);

            if (arg.equals("myads")) {
                return Cookie.cookie("myads", uuid);
            }

            return null;
        });

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        assertThat(dataContext.getUserId(), is(uuid));
        assertThat(dataContext.isNewUserId(), is(false));
    }

    @Test
    public void testMalformedCookie() {
        when(request.getCookie(anyString()))
                .thenReturn(Cookie.cookie("sharedid", "malformed"));

        handler.handle(routingContext);

        DataContext dataContext = DataContext.from(routingContext);

        assertThat(dataContext.getUserId(), is(not("malformed")));
        assertThat(dataContext.isNewUserId(), is(true));
    }
}
