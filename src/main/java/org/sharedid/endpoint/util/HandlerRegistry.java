package org.sharedid.endpoint.util;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class HandlerRegistry {
    private HashMap<Class<Handler<RoutingContext>>, Handler<RoutingContext>> handlers = new HashMap<>();

    public HandlerRegistry(Collection<Handler<RoutingContext>> handlers) {
        handlers.forEach(this::add);
    }

    public HandlerRegistry() { }

    public <T extends Handler<RoutingContext>> Handler<RoutingContext> get(Class<T> clazz) throws NoSuchElementException {
        Handler<RoutingContext> handler = handlers.get(clazz);
        if (handler == null) {
            throw new NoSuchElementException("Handler for key " + clazz + " doesn't exist");
        } else {
            return handler;
        }
    }

    public <T extends Handler<RoutingContext>> void add(T handler) {
        Class<Handler<RoutingContext>> clazz = (Class<Handler<RoutingContext>>) handler.getClass();
        this.handlers.put(clazz, handler);
    }
}
