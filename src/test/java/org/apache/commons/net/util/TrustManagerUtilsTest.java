package org.apache.commons.net.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TrustManagerUtils}.
 */
public class TrustManagerUtilsTest {

    @Test
    public void testGetAcceptAllTrustManager() {
        assertNotNull(TrustManagerUtils.getAcceptAllTrustManager());
    }

    @Test
    public void testGetDefaultTrustManager() throws KeyStoreException, GeneralSecurityException {
        assertNotNull(TrustManagerUtils.getDefaultTrustManager(KeyStore.getInstance(KeyStore.getDefaultType())));
    }

    @Test
    public void testGetValidateServerCertificateTrustManager() {
        assertNotNull(TrustManagerUtils.getValidateServerCertificateTrustManager());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testToConstructor() {
        assertDoesNotThrow(TrustManagerUtils::new);
    }

}
