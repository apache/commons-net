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
package org.apache.commons.net.ntp;

import org.junit.Test;
import org.junit.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TestTimeInfo {

    @Test
    public void testEquals() {
        NtpV3Packet packet = new NtpV3Impl();
        final long returnTime = System.currentTimeMillis();
        TimeInfo info = new TimeInfo(packet, returnTime);
        info.addComment("this is a comment");
        TimeInfo other = new TimeInfo(packet, returnTime);
        other.addComment("this is a comment");
        Assert.assertEquals(info, other); // fails
        Assert.assertEquals(info.hashCode(), other.hashCode());
        other.addComment("another comment");
        //Assert.assertFalse(info.equals(other)); // comments not used for equality

        TimeInfo another = new TimeInfo(packet, returnTime, new ArrayList<String>());
        Assert.assertEquals(info, another);
    }

    @Test
    public void testComputeDetails() {
        // if (origTime > returnTime) // assert destTime >= origTime
        NtpV3Packet packet = new NtpV3Impl();
        long returnTime = System.currentTimeMillis();

        // example
        // returntime=1370571658178
        // origTime=  1370571659178

        // originate time as defined in RFC-1305 (t1)
        packet.setOriginateTimeStamp(TimeStamp.getNtpTime(returnTime + 1000));
        // Receive Time is time request received by server (t2)
        packet.setReceiveTimeStamp(packet.getOriginateTimeStamp());
        // Transmit time is time reply sent by server (t3)
        packet.setTransmitTime(packet.getOriginateTimeStamp());
        packet.setReferenceTime(packet.getOriginateTimeStamp());

        //long origTime = packet.getOriginateTimeStamp().getTime();
        //System.out.println("returntime=" + returnTime);
        //System.out.println("origTime=  " + origTime);

        TimeInfo info = new TimeInfo(packet, returnTime);
        info.computeDetails();

        Assert.assertSame(packet, info.getMessage());
        Assert.assertEquals(returnTime, info.getReturnTime());
        Assert.assertEquals(Long.valueOf(500), info.getOffset());
        Assert.assertEquals(Long.valueOf(-1000), info.getDelay());

        // comments: [Warning: processing time > total network time, Error: OrigTime > DestRcvTime]
        Assert.assertEquals(2, info.getComments().size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testException() {
        NtpV3Packet packet = null;
        new TimeInfo(packet, 1L);
    }

    @Test
    public void testAddress() throws UnknownHostException {
        NtpV3Packet packet = new NtpV3Impl();
        TimeInfo info = new TimeInfo(packet, System.currentTimeMillis());
        Assert.assertNull(info.getAddress());
        packet.getDatagramPacket().setAddress(InetAddress.getByAddress("loopback", new byte[]{127, 0, 0, 1}));
        Assert.assertNotNull(info.getAddress());
    }

    @Test
    public void testZeroTime() {
        NtpV3Packet packet = new NtpV3Impl();
        TimeInfo info = new TimeInfo(packet, 0);
        info.computeDetails();
        Assert.assertNull(info.getDelay());
        Assert.assertNull(info.getOffset());
        Assert.assertEquals(0L, info.getReturnTime());
        // comments: Error: zero orig time -- cannot compute delay/offset
        final List<String> comments = info.getComments();
        Assert.assertEquals(1, comments.size());
        Assert.assertTrue(comments.get(0).contains("zero orig time"));
    }

    @Test
    public void testNotEquals() {
        NtpV3Packet packet = new NtpV3Impl();
        long returnTime = System.currentTimeMillis();
        TimeInfo info = new TimeInfo(packet, returnTime);

        // 1. different return time
        NtpV3Packet packet2 = new NtpV3Impl();
        Assert.assertEquals(packet, packet2);
        TimeInfo info2 = new TimeInfo(packet2, returnTime + 1);
        Assert.assertFalse(info.equals(info2));

        // 2. different message / same time
        packet2.setStratum(3);
        packet2.setRootDelay(25);
        TimeInfo info3 = new TimeInfo(packet2, returnTime);
        Assert.assertFalse(info.equals(info3));

        // 3. different class
        Object  other = this;
        Assert.assertFalse(info.equals(other));

        // 4. null comparison
        other = null;
        Assert.assertFalse(info.equals(other));
    }

}
