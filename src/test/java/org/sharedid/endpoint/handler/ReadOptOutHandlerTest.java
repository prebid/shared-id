package org.sharedid.endpoint.handler;

import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ReadOptOutHandlerTest {
    private ReadOptOutHandler handler;

    @Mock
    private RoutingContext routingContext;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        handler = new ReadOptOutHandler("optout", "legacy");
    }

    @Test
    public void testNoCookies() {
        when(routingContext.getCookie(anyString())).thenReturn(null);

        DataContext dataContext = DataContext.from(routingContext);

        handler.handle(routingContext);

        assertThat(dataContext.getHasOptOutCookie(), is(false));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testOptOutCookie() {
        when(routingContext.getCookie(anyString())).thenAnswer(invocation -> {
            if (invocation.getArgument(0) == "optout") {
                return Cookie.cookie("optout", "value");
            }

            return null;
        });

        DataContext dataContext = DataContext.from(routingContext);

        handler.handle(routingContext);

        assertThat(dataContext.getHasOptOutCookie(), is(true));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testLegacyCookie() {
        when(routingContext.getCookie(anyString())).thenAnswer(invocation -> {
            if (invocation.getArgument(0) == "legacy") {
                return Cookie.cookie("legacy", "value");
            }

            return null;
        });

        DataContext dataContext = DataContext.from(routingContext);

        handler.handle(routingContext);

        assertThat(dataContext.getHasOptOutCookie(), is(true));

        verify(routingContext, times(1)).next();
    }
}
