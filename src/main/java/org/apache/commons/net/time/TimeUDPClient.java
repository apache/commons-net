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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.net.DatagramSocketClient;

/**
 * The TimeUDPClient class is a UDP implementation of a client for the Time protocol described in RFC 868. To use the class, merely open a local datagram socket
 * with {@link org.apache.commons.net.DatagramSocketClient#open open } and call {@link #getTime getTime } or {@link #getTime getDate } to retrieve the time.
 * Then call {@link org.apache.commons.net.DatagramSocketClient#close close } to close the connection properly. Unlike
 * {@link org.apache.commons.net.time.TimeTCPClient}, successive calls to {@link #getTime getTime } or {@link #getDate getDate } are permitted without
 * re-establishing a connection. That is because UDP is a connectionless protocol and the Time protocol is stateless.
 *
 *
 * @see TimeTCPClient
 */

public final class TimeUDPClient extends DatagramSocketClient {
    /** The default time port. It is set to 37 according to RFC 868. */
    public static final int DEFAULT_PORT = 37;

    /**
     * The number of seconds between 00:00 1 January 1900 and 00:00 1 January 1970. This value can be useful for converting time values to other formats.
     */
    public static final long SECONDS_1900_TO_1970 = 2208988800L;

    private final byte[] dummyData = new byte[1];
    private final byte[] timeData = new byte[4];

    /**
     * Same as <code> getTime(host, DEFAULT_PORT); </code>
     *
     * @param host the time-server
     * @return the date
     * @throws IOException on error
     */
    public Date getDate(final InetAddress host) throws IOException {
        return new Date((getTime(host, DEFAULT_PORT) - SECONDS_1900_TO_1970) * 1000L);
    }

    /**
     * Retrieves the time from the server and returns a Java Date containing the time converted to the local time zone.
     *
     * @param host The address of the server.
     * @param port The port of the service.
     * @return A Date value containing the time retrieved from the server converted to the local time zone.
     * @throws IOException If an error occurs while fetching the time.
     */
    public Date getDate(final InetAddress host, final int port) throws IOException {
        return new Date((getTime(host, port) - SECONDS_1900_TO_1970) * 1000L);
    }

    /**
     * Same as <code> getTime(host, DEFAULT_PORT); </code>
     *
     * @param host the time-server
     * @return the time returned from the server
     * @throws IOException on error
     */
    public long getTime(final InetAddress host) throws IOException {
        return getTime(host, DEFAULT_PORT);
    }

    /**
     * Retrieves the time from the specified server and port and returns it. The time is the number of seconds since 00:00 (midnight) 1 January 1900 GMT, as
     * specified by RFC 868. This method reads the raw 32-bit big-endian unsigned integer from the server, converts it to a Java long, and returns the value.
     *
     * @param host The address of the server.
     * @param port The port of the service.
     * @return The time value retrieved from the server.
     * @throws IOException If an error occurs while retrieving the time.
     */
    public long getTime(final InetAddress host, final int port) throws IOException {
        final DatagramPacket sendPacket = new DatagramPacket(dummyData, dummyData.length, host, port);
        final DatagramPacket receivePacket = new DatagramPacket(timeData, timeData.length);

        checkOpen().send(sendPacket);
        checkOpen().receive(receivePacket);

        long time = 0L;
        time |= (timeData[0] & 0xff) << 24 & 0xffffffffL;
        time |= (timeData[1] & 0xff) << 16 & 0xffffffffL;
        time |= (timeData[2] & 0xff) << 8 & 0xffffffffL;
        time |= timeData[3] & 0xff & 0xffffffffL;

        return time;
    }

}
