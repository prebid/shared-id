package org.sharedid.endpoint.util;

import java.util.UUID;

public class IdGeneratorUtils {

    private IdGeneratorUtils() {
    }

    public static String generatePubcid() {
        return UUID.randomUUID().toString();
    }
}
