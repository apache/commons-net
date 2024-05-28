package org.apache.commons.net.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TrustManagerUtils}.
 */
public class TrustManagerUtilsTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testToConstructor() {
        assertDoesNotThrow(TrustManagerUtils::new);
    }

}
