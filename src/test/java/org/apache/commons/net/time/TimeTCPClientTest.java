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

import java.net.InetAddress;
import java.util.Calendar;
import java.io.IOException;
import java.util.TimeZone;

import junit.framework.TestCase;

public class TimeTCPClientTest extends TestCase
{
    private TimeTestSimpleServer server1;

    private int _port = 3333; // default test port

    protected void openConnections() throws Exception
    {
        try {
            server1 = new TimeTestSimpleServer(_port);
            server1.connect();
        } catch (IOException ioe)
        {
            // try again on another port
            _port = 4000;
            server1 = new TimeTestSimpleServer(_port);
            server1.connect();
        }
        server1.start();
    }

    /*
     *  tests the constant basetime used by TimeClient against tha
     *  computed from Calendar class.
     */
    public void testInitial() {
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(utcZone);
        calendar.set(1900, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long baseTime = calendar.getTime().getTime() / 1000L;

        assertEquals(baseTime, -TimeTCPClient.SECONDS_1900_TO_1970);
    }

    /*
     * tests the times retrieved via the Time protocol implementation.
     */
    public void testCompareTimes() throws Exception
    {
        openConnections();

        long time, time2;
        long clientTime, clientTime2;
        TimeTCPClient client = new TimeTCPClient();
        try
        {
            // Not sure why code used to use getLocalHost.
            final InetAddress localHost = InetAddress.getByName("localhost"); // WAS InetAddress.getLocalHost();
            try
            {
                // We want to timeout if a response takes longer than 60 seconds
                client.setDefaultTimeout(60000);
                client.connect(localHost, _port);
                clientTime = client.getDate().getTime();
                time = System.currentTimeMillis();
            } catch (IOException e) { // catch the first connect error; assume second will work if this does
                fail("IOError <"+e+"> trying to connect to " + localHost + " " + _port );
                throw e;
            } finally
            {
              if(client.isConnected()) {
                  client.disconnect();
              }
            }

            try
            {
                // We want to timeout if a response takes longer than 60 seconds
                client.setDefaultTimeout(60000);
                client.connect(localHost, _port);
                clientTime2 = (client.getTime() - TimeTCPClient.SECONDS_1900_TO_1970)*1000L;
                time2 = System.currentTimeMillis();
            } finally
            {
              if(client.isConnected()) {
                  client.disconnect();
              }
            }
        } finally
        {
            closeConnections();
        }

        // current time shouldn't differ from time reported via network by 5 seconds
        assertTrue(Math.abs(time - clientTime) < 5000);
        assertTrue(Math.abs(time2 - clientTime2) < 5000);
    }

    protected void closeConnections()
    {
        try
        {
            server1.stop();
            Thread.sleep(1000);
        } catch (Exception e)
        {
            // ignored
        }
    }
}

