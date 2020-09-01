package org.sharedid.endpoint.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class RespondSharedIdHandlerTest {
    private RespondSharedIdHandler handler;

    @Mock
    private RoutingContext routingContext;

    @Mock
    private HttpServerResponse response;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);
        when(routingContext.response()).thenReturn(response);

        handler = new RespondSharedIdHandler();
    }

    @Test
    public void testRespond() {
        when(response.getStatusCode()).thenReturn(200);

        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setUserId("userid");

        handler.handle(routingContext);

        ArgumentCaptor<Buffer> captor = ArgumentCaptor.forClass(Buffer.class);
        verify(response, times(1)).end(captor.capture());

        Buffer buffer = captor.getValue();

        JsonObject json = new JsonObject(buffer);

        assertThat(json.getString("sharedId"), is("userid"));
    }
}
