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

package org.apache.commons.net.tftp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TFTPRequestPacket}.
 */
class TFTPRequestPacketOptionBoundsTest {

    /**
     * RRQ for "f" in octet mode, one option whose value has no terminating NUL.
     */
    private static byte[] newReadRequest() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0);
        out.write(TFTPPacket.READ_REQUEST);
        out.write("f".getBytes(StandardCharsets.US_ASCII));
        out.write(0);
        out.write("octet".getBytes(StandardCharsets.US_ASCII));
        out.write(0);
        out.write("blksize".getBytes(StandardCharsets.US_ASCII));
        out.write(0);
        out.write("1024".getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private static String parse(final byte[] buf, final int len) throws TFTPPacketException {
        final DatagramPacket packet = new DatagramPacket(buf, len, InetAddress.getLoopbackAddress(), 69);
        return "OK " + ((TFTPRequestPacket) TFTPPacket.newTFTPPacket(packet)).getOptions();
    }

    @Test
    void testParseIsIndependentOfBytesBeyondGetLength() throws Exception {
        final byte[] request = newReadRequest();
        final byte[] zeroed = Arrays.copyOf(request, request.length + 16);
        final byte[] stale = Arrays.copyOf(request, request.length + 16);
        Arrays.fill(stale, request.length, stale.length, (byte) 'S');
        assertThrowsExactly(TFTPPacketException.class, () -> parse(zeroed, request.length));
        assertThrowsExactly(TFTPPacketException.class, () -> parse(stale, request.length));
    }

    @Test
    void testUnterminatedOptionAtEndOfBufferThrowsDeclaredException() throws Exception {
        final byte[] request = newReadRequest();
        final DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getLoopbackAddress(), 69);
        assertThrows(TFTPPacketException.class, () -> TFTPPacket.newTFTPPacket(packet));
    }
}
