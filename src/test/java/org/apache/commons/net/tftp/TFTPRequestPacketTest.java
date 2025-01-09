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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests {@link TFTPRequestPacket}.
 */
class TFTPRequestPacketTest {

    @Test
    public void testGetOptions() throws UnknownHostException, TFTPPacketException {
        DatagramPacket datagramPacket = getDatagramPacket();
        TFTPReadRequestPacket requestPacket = new TFTPReadRequestPacket(datagramPacket);
        assertNotNull(requestPacket.toString());
        Map<String, String> options = requestPacket.getOptions();
        assertEquals(1, options.size());
        assertEquals("1024", options.get("blksize"));
    }

    @Test
    public void testNewDatagram() throws TFTPPacketException, UnknownHostException {
        DatagramPacket datagramPacket = getDatagramPacket();

        TFTPReadRequestPacket requestPacket = new TFTPReadRequestPacket(datagramPacket);
        DatagramPacket newDatagram = requestPacket.newDatagram();

        assertNotNull(newDatagram);
        assertEquals(datagramPacket.getAddress(), newDatagram.getAddress());
        assertEquals(datagramPacket.getPort(), newDatagram.getPort());
        assertEquals(datagramPacket.getLength(), newDatagram.getLength());
        assertArrayEquals(datagramPacket.getData(), newDatagram.getData());

        byte[] data = new byte[datagramPacket.getLength()];
        DatagramPacket newDatagram2 = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 0);
        requestPacket.newDatagram(newDatagram2, data);

        assertEquals(datagramPacket.getAddress(), newDatagram2.getAddress());
        assertEquals(datagramPacket.getPort(), newDatagram2.getPort());
        assertArrayEquals(datagramPacket.getData(), data);
    }

    private static DatagramPacket getDatagramPacket() throws UnknownHostException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(0);
        byteStream.write(1);

        try {
            byteStream.write("fileName".getBytes(StandardCharsets.US_ASCII));
            byteStream.write(0);
            byteStream.write("octet".getBytes(StandardCharsets.US_ASCII));
            byteStream.write(0);

            byteStream.write("blksize".getBytes(StandardCharsets.US_ASCII));
            byteStream.write(0);
            byteStream.write("1024".getBytes(StandardCharsets.US_ASCII));
            byteStream.write(0);
        } catch (IOException e) {
            throw new RuntimeException("Error creating TFTP request packet", e);
        }

        byte[] data = byteStream.toByteArray();
        return new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 0);
    }
}
