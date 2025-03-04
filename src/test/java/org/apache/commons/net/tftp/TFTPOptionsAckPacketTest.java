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

package org.apache.commons.net.tftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TFTPOptionsAckPacketTest {
    private static final Map<String, String> options = new HashMap<>(1);
    static {
        options.put("blksize", "1024");
    }

    @Test
    void getOptions() throws UnknownHostException {
        final TFTPOptionsAckPacket packet = new TFTPOptionsAckPacket(InetAddress.getLocalHost(), 0, options);
        assertEquals(options, packet.getOptions());
    }

    @Test
    void newDatagram() throws UnknownHostException {
        final TFTPOptionsAckPacket packet = new TFTPOptionsAckPacket(InetAddress.getLocalHost(), 0, options);
        assertNotNull(packet.newDatagram());
    }

    @Test
    void testNewDatagram() throws UnknownHostException {
        final TFTPOptionsAckPacket packet = new TFTPOptionsAckPacket(InetAddress.getLocalHost(), 0, options);
        final DatagramPacket datagramPacket = packet.newDatagram();
        final byte[] data = new byte[datagramPacket.getLength()];
        final DatagramPacket newDatagram2 = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 0);
        packet.newDatagram(newDatagram2, data);

        assertEquals(datagramPacket.getAddress(), newDatagram2.getAddress());
        assertEquals(datagramPacket.getPort(), newDatagram2.getPort());
        assertArrayEquals(datagramPacket.getData(), data);
    }
}