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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

abstract class TFTPPacketTest {

    protected abstract Executable getDatagramPacketCtor(DatagramPacket packet);

    @Test
    public void testConstructorBadType() throws UnknownHostException {
        // Create a DatagramPacket with invalid TFTP packet type (not ACK)
        final InetAddress address = InetAddress.getLocalHost();
        final byte[] data = new byte[4];
        data[0] = 0; // TFTP opcode 0 (invalid)
        data[1] = 0; // TFTP opcode 0 (invalid)
        data[2] = 0; // Block number high byte
        data[3] = 1; // Block number low byte
        final DatagramPacket packet = new DatagramPacket(data, data.length, address, 69);
        assertThrows(TFTPPacketException.class, () -> TFTPPacket.newTFTPPacket(packet));
        assertThrows(TFTPPacketException.class, getDatagramPacketCtor(packet));
    }
}
