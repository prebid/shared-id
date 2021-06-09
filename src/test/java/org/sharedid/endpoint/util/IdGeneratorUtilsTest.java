package org.sharedid.endpoint.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IdGeneratorUtilsTest {
    @Test
    void testIdGenerator() {
        String id = IdGeneratorUtils.generatePubcid();
        Assertions.assertThat(id.length()).isEqualTo(36);
    }
}
