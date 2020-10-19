package org.sharedid.endpoint.handler;

import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sharedid.endpoint.context.DataContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({
        MockitoExtension.class
})
public class ParseGdprConsentStringHandlerTest {
    private ParseGdprConsentStringHandler handler;

    @Mock
    private RoutingContext routingContext;

    private Map<String, Object> data;

    @BeforeEach
    public void setup() {
        data = new HashMap<>();

        when(routingContext.data()).thenReturn(data);

        handler = new ParseGdprConsentStringHandler();
    }

    @Test
    public void testMissingGdprConsentStringParam() {
        DataContext dataContext = DataContext.from(routingContext);

        handler.handle(routingContext);

        assertThat(dataContext.getGdprConsentString(), is(nullValue()));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testBlankGdprConsentStringParam() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setGdprConsentParam("");

        handler.handle(routingContext);

        assertThat(dataContext.getGdprConsentString(), is(nullValue()));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testMalformedGdprString() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setGdprConsentParam("malformed");

        handler.handle(routingContext);

        assertThat(dataContext.getGdprConsentString(), is(nullValue()));

        verify(routingContext, times(1)).next();
    }

    @Test
    public void testValidGdprString() {
        DataContext dataContext = DataContext.from(routingContext);
        dataContext.setGdprConsentParam("COuQACgOuQACgM-AAAENAPCAAIAAAIAAAAAAAjQAQAaACNABABoACEgAgA0A");

        handler.handle(routingContext);

        assertThat(dataContext.getGdprConsentString(), is(notNullValue()));

        verify(routingContext, times(1)).next();
    }
}
