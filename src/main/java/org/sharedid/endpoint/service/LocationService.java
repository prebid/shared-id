package org.sharedid.endpoint.service;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;

import java.net.InetAddress;
import java.util.Optional;

public interface LocationService {
    Future<String> getCountryForAddress(InetAddress address);
    Future<String> getCountryForRequest(HttpServerRequest request);

    Future<Optional<String>> getStateForAddress(InetAddress address);
    Future<Optional<String>> getStateForRequest(HttpServerRequest request);
}
