/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
