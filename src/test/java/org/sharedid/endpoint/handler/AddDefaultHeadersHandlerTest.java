package org.sharedid.endpoint.handler;


import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class AddDefaultHeadersHandlerTest {
    private AddDefaultHeadersHandler handler;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    @BeforeEach
    public void setup() {
        when(routingContext.response()).thenReturn(response);

        handler = new AddDefaultHeadersHandler();
    }

    @Test
    public void testDefaultHeadersAdded() {
        handler.handle(routingContext);

        verify(response, times(1))
                .putHeader("P3P", "CP=\"NOI CURa ADMa DEVa TAIa OUR BUS IND UNI COM NAV INT\"");
        verify(response, times(1)).putHeader("Pragma", "no-cache");
        verify(response, times(1)).putHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        verify(response, times(1)).putHeader("Expires", "0");

        verify(routingContext, times(1)).next();
    }
}
