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
package org.apache.commons.net.ntp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestTimeInfo {

    @Test
    public void testAddress() throws UnknownHostException {
        final NtpV3Packet packet = new NtpV3Impl();
        final TimeInfo info = new TimeInfo(packet, System.currentTimeMillis());
        assertNull(info.getAddress());
        packet.getDatagramPacket().setAddress(InetAddress.getByAddress("loopback", new byte[] { 127, 0, 0, 1 }));
        assertNotNull(info.getAddress());
    }

    @Test
    public void testComputeDetails() {
        // if (origTime > returnTime) // assert destTime >= origTime
        final NtpV3Packet packet = new NtpV3Impl();
        final long returnTimeMillis = System.currentTimeMillis();

        // example
        // returntime=1370571658178
        // origTime= 1370571659178

        // originate time as defined in RFC-1305 (t1)
        packet.setOriginateTimeStamp(TimeStamp.getNtpTime(returnTimeMillis + 1000));
        // Receive Time is time request received by server (t2)
        packet.setReceiveTimeStamp(packet.getOriginateTimeStamp());
        // Transmit time is time reply sent by server (t3)
        packet.setTransmitTime(packet.getOriginateTimeStamp());
        packet.setReferenceTime(packet.getOriginateTimeStamp());

        // long origTime = packet.getOriginateTimeStamp().getTime();
        // System.out.println("returntime=" + returnTime);
        // System.out.println("origTime= " + origTime);

        final TimeInfo info = new TimeInfo(packet, returnTimeMillis);
        info.computeDetails();

        assertSame(packet, info.getMessage());
        assertEquals(returnTimeMillis, info.getReturnTime());
        assertEquals(Long.valueOf(500), info.getOffset());
        assertEquals(Long.valueOf(-1000), info.getDelay());

        // comments: [Warning: processing time > total network time, Error: OrigTime > DestRcvTime]
        assertEquals(2, info.getComments().size());
    }

    @Test
    public void testEquals() {
        final NtpV3Packet packet = new NtpV3Impl();
        final long returnTime = System.currentTimeMillis();
        final TimeInfo info = new TimeInfo(packet, returnTime);
        info.addComment("this is a comment");
        final TimeInfo other = new TimeInfo(packet, returnTime);
        other.addComment("this is a comment");
        assertEquals(info, other); // fails
        assertEquals(info.hashCode(), other.hashCode());
        other.addComment("another comment");
        // Assertions.assertFalse(info.equals(other)); // comments not used for equality

        final TimeInfo another = new TimeInfo(packet, returnTime, new ArrayList<>());
        assertEquals(info, another);
    }

    @Test
    public void testException() {
        final NtpV3Packet packet = null;
        assertThrows(IllegalArgumentException.class, () -> new TimeInfo(packet, 1L));
    }

    @Test
    public void testNotEquals() {
        final NtpV3Packet packet = new NtpV3Impl();
        final long returnTime = System.currentTimeMillis();
        final TimeInfo info = new TimeInfo(packet, returnTime);

        // 1. different return time
        final NtpV3Packet packet2 = new NtpV3Impl();
        assertEquals(packet, packet2);
        final TimeInfo info2 = new TimeInfo(packet2, returnTime + 1);
        assertNotEquals(info, info2);

        // 2. different message / same time
        packet2.setStratum(3);
        packet2.setRootDelay(25);
        final TimeInfo info3 = new TimeInfo(packet2, returnTime);
        assertNotEquals(info, info3);

        // 3. different class
        Object other = this;
        assertNotEquals(info, other);

        // 4. null comparison
        other = null;
        assertNotEquals(info, other);
    }

    @Test
    public void testZeroTime() {
        final NtpV3Packet packet = new NtpV3Impl();
        final TimeInfo info = new TimeInfo(packet, 0);
        info.computeDetails();
        assertNull(info.getDelay());
        assertNull(info.getOffset());
        assertEquals(0L, info.getReturnTime());
        // comments: Error: zero orig time -- cannot compute delay/offset
        final List<String> comments = info.getComments();
        assertEquals(1, comments.size());
        assertTrue(comments.get(0).contains("zero orig time"));
    }

}
