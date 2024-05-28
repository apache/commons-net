/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.finger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link FingerClient}.
 */
public class FingerClientTest {

    @Test
    public void testConstructor() {
        assertDoesNotThrow(FingerClient::new);
    }

    @Test
    public void testDefaultPort() {
        assertEquals(FingerClient.DEFAULT_PORT, new FingerClient().getDefaultPort());
    }

    @Test
    public void testDisconnect() throws IOException {
        new FingerClient().disconnect();
    }

    @Test
    public void testGetInputStream() {
        final FingerClient fingerClient = new FingerClient();
        // Not connected throws NullPointerException
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(false));
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(true));
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(false, ""));
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(true, ""));
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(false, "", null));
        assertThrows(NullPointerException.class, () -> fingerClient.getInputStream(true, "", null));
    }

    @Test
    public void testQuery() {
        final FingerClient fingerClient = new FingerClient();
        // Not connected throws NullPointerException
        assertThrows(NullPointerException.class, () -> fingerClient.query(false));
        assertThrows(NullPointerException.class, () -> fingerClient.query(true));
        assertThrows(NullPointerException.class, () -> fingerClient.query(false, ""));
        assertThrows(NullPointerException.class, () -> fingerClient.query(true, ""));
    }

}
