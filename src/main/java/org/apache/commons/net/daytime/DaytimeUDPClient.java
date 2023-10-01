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

package org.apache.commons.net.daytime;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.apache.commons.net.DatagramSocketClient;

/**
 * The DaytimeUDPClient class is a UDP implementation of a client for the Daytime protocol described in RFC 867. To use the class, merely open a local datagram
 * socket with {@link org.apache.commons.net.DatagramSocketClient#open open } and call {@link #getTime getTime } to retrieve the daytime string, then call
 * {@link org.apache.commons.net.DatagramSocketClient#close close } to close the connection properly. Unlike
 * {@link org.apache.commons.net.daytime.DaytimeTCPClient}, successive calls to {@link #getTime getTime } are permitted without re-establishing a connection.
 * That is because UDP is a connectionless protocol and the Daytime protocol is stateless.
 *
 * @see DaytimeTCPClient
 */
public final class DaytimeUDPClient extends DatagramSocketClient {
    /** The default daytime port. It is set to 13 according to RFC 867. */
    public static final int DEFAULT_PORT = 13;

    private final byte[] dummyData = new byte[1];
    // Received dates should be less than 256 bytes
    private final byte[] timeData = new byte[256];

    /**
     * Same as <code>getTime(host, DaytimeUDPClient.DEFAULT_PORT);</code>
     *
     * @param host the host
     * @return the time
     * @throws IOException on error
     */
    public String getTime(final InetAddress host) throws IOException {
        return getTime(host, DEFAULT_PORT);
    }

    /**
     * Retrieves the time string from the specified server and port and returns it.
     *
     * @param host The address of the server.
     * @param port The port of the service.
     * @return The time string.
     * @throws IOException If an error occurs while retrieving the time.
     */
    public String getTime(final InetAddress host, final int port) throws IOException {
        final DatagramPacket sendPacket;
        final DatagramPacket receivePacket;

        sendPacket = new DatagramPacket(dummyData, dummyData.length, host, port);
        receivePacket = new DatagramPacket(timeData, timeData.length);

        checkOpen().send(sendPacket);
        checkOpen().receive(receivePacket);

        return new String(receivePacket.getData(), 0, receivePacket.getLength(), getCharset()); // Java 1.6 can use getCharset()
    }

}
