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

package org.apache.commons.net.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TimeUDPClient}.
 */
public class TimeUDPClientTest {

    @Test
    public void testConstructor() {
        try (TimeUDPClient client = new TimeUDPClient()) {
            // empty
        }
    }

    @Test
    public void testGetDate() {
        try (TimeUDPClient client = new TimeUDPClient()) {
            // Not connected failures
            assertThrows(NullPointerException.class, () -> client.getDate(InetAddress.getLocalHost()));
            assertThrows(NullPointerException.class, () -> client.getDate(InetAddress.getLocalHost(), TimeUDPClient.DEFAULT_PORT));
        }
    }

    @Test
    public void testGetTime() {
        try (TimeUDPClient client = new TimeUDPClient()) {
            // Not connected failures
            assertThrows(NullPointerException.class, () -> client.getTime(InetAddress.getLocalHost()));
            assertThrows(NullPointerException.class, () -> client.getTime(InetAddress.getLocalHost(), TimeUDPClient.DEFAULT_PORT));
        }
    }

    @Test
    public void testToTime() {
        final byte[] timeData = new byte[4];
        assertEquals(0, TimeUDPClient.toTime(timeData));
    }
}
