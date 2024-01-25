/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the getters on {@link FTPSClient}.
 */
public class FTPSClientGettersTest {

    @Test
    public void testGetters() {
        final FTPSClient testClient = new FTPSClient("SSL", true);
        assertTrue(testClient.isImplicit());
        assertEquals("SSL", testClient.getProtocol());

        final FTPSClient testClient2 = new FTPSClient("TLS", false);
        assertFalse(testClient2.isImplicit());
        assertEquals("TLS", testClient2.getProtocol());
        final String[] protocols = {"123", "456"};
        testClient2.setEnabledProtocols(protocols);
        assertArrayEquals(protocols, testClient2.getProtocols());
        testClient2.setNeedClientAuth(true);
        assertTrue(testClient2.isNeedClientAuth());
        testClient2.setNeedClientAuth(false);
        assertFalse(testClient2.isNeedClientAuth());
        testClient2.setWantClientAuth(true);
        assertTrue(testClient2.isWantClientAuth());
        testClient2.setWantClientAuth(false);
        assertFalse(testClient2.isWantClientAuth());
        final String[] suites = {"abc", "def"};
        testClient2.setEnabledCipherSuites(suites);
        assertArrayEquals(suites, testClient2.getSuites());
        testClient2.setAuthValue("qwerty");
        assertEquals("qwerty", testClient2.getAuthValue());
        testClient2.setUseClientMode(true);
        assertTrue(testClient2.isClientMode());
        testClient2.setUseClientMode(false);
        assertFalse(testClient2.isClientMode());
        testClient2.setEnabledSessionCreation(true);
        assertTrue(testClient2.isCreation());
        testClient2.setEnabledSessionCreation(false);
        assertFalse(testClient2.isCreation());
    }
}

