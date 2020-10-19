package org.sharedid.endpoint.util;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;

public class ResponseUtil {
    private ResponseUtil () {}

    public static void redirect(HttpServerResponse response, String redirectUrl) {
        response.setStatusCode(302);
        response.putHeader("Location", redirectUrl);
        response.end();
    }

    public static void noContent(HttpServerResponse response) {
        response.setStatusCode(204);
        response.end();
    }

    public static void badRequest(HttpServerResponse response) {
        response.setStatusCode(400);
        response.end();
    }
}

