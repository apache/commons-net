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

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;

import org.apache.commons.net.examples.ntp.SimpleNTPServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class for NtpClient using SimpleNTPServer
 */
public class TestNtpClient {

    private static SimpleNTPServer server;

    @BeforeClass
    public static void oneTimeSetUp() throws IOException {
        // one-time initialization code
        server = new SimpleNTPServer(0);
        server.connect();

        try {
            server.start();
        } catch (final IOException e) {
            Assert.fail("failed to start NTP server: " + e);
        }
        Assert.assertTrue(server.isStarted());
        // System.out.println("XXX: time server started");
        boolean running = false;
        for (int retries = 0; retries < 5; retries++) {
            running = server.isRunning();
            if (running) {
                break;
            }
            // if not running then sleep 2 seconds and try again
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // ignore
            }
        }
        Assert.assertTrue(running);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // one-time cleanup code
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void testGetTime() throws IOException {
        final long currentTimeMillis = System.currentTimeMillis();
        final NTPUDPClient client = new NTPUDPClient();
        // timeout if response takes longer than 2 seconds
        client.setDefaultTimeout(Duration.ofSeconds(2));
        try {
            // Java 1.7: use InetAddress.getLoopbackAddress() instead
            final InetAddress addr = InetAddress.getByAddress("loopback", new byte[] { 127, 0, 0, 1 });
            final TimeInfo timeInfo = client.getTime(addr, server.getPort());
            Assert.assertNotNull(timeInfo);
            Assert.assertTrue(timeInfo.getReturnTime() >= currentTimeMillis);
            final NtpV3Packet message = timeInfo.getMessage();
            Assert.assertNotNull(message);

            final TimeStamp rcvTimeStamp = message.getReceiveTimeStamp();
            final TimeStamp xmitTimeStamp = message.getTransmitTimeStamp();
            Assert.assertTrue(xmitTimeStamp.compareTo(rcvTimeStamp) >= 0);

            final TimeStamp originateTimeStamp = message.getOriginateTimeStamp();
            Assert.assertNotNull(originateTimeStamp);
            Assert.assertTrue(originateTimeStamp.getTime() >= currentTimeMillis);

            Assert.assertEquals(NtpV3Packet.MODE_SERVER, message.getMode());

            // following assertions are specific to the SimpleNTPServer

            final TimeStamp referenceTimeStamp = message.getReferenceTimeStamp();
            Assert.assertNotNull(referenceTimeStamp);
            Assert.assertTrue(referenceTimeStamp.getTime() >= currentTimeMillis);

            Assert.assertEquals(NtpV3Packet.VERSION_3, message.getVersion());
            Assert.assertEquals(1, message.getStratum());

            Assert.assertEquals("LCL", NtpUtils.getReferenceClock(message));
        } finally {
            client.close();
        }
    }

}
