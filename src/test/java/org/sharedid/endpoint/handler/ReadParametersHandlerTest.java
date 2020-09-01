package org.sharedid.endpoint.handler;

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ReadParametersHandlerTest {
    private ReadParametersHandler handler;

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

        handler = new ReadParametersHandler();
    }

    @Test
    public void testParams() {
        when(request.getParam(anyString())).thenAnswer(invocation -> {
            String param = invocation.getArgument(0);

            if (param.equals(ReadParametersHandler.PARAM_VENDOR)) {
                return "0";
            }

            return param;
        });

        DataContext dataContext = DataContext.from(routingContext);

        handler.handle(routingContext);

        assertThat(dataContext.isGdprParam(), is("gdpr"));
        assertThat(dataContext.getGdprConsentParam(), is("gdpr_consent"));
        assertThat(dataContext.getUsPrivacyParam(), is("us_privacy"));
        assertThat(dataContext.getVendor(), is(0));
        assertThat(dataContext.getRedirectUrlParam(), is("redir"));

        verify(routingContext, times(1)).next();
    }
}
