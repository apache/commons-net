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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link TFTPDataPacket}.
 */
public class TFTPDataPacketTest {

    @Test
    public void testNewDatagram() throws UnknownHostException {
        assertNotNull(new TFTPDataPacket(InetAddress.getLocalHost(), 0, 0, new byte[0]).newDatagram());
    }

    @Test
    public void testToString() throws UnknownHostException {
        assertNotNull(new TFTPDataPacket(InetAddress.getLocalHost(), 0, 0, new byte[0]).toString());
    }
}
