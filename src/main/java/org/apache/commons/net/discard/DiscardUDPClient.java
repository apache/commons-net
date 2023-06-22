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

package org.apache.commons.net.discard;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.apache.commons.net.DatagramSocketClient;
import org.apache.commons.net.util.NetConstants;

/**
 * The DiscardUDPClient class is a UDP implementation of a client for the Discard protocol described in RFC 863. To use the class, just open a local UDP port
 * with {@link org.apache.commons.net.DatagramSocketClient#open open } and call {@link #send send } to send datagrams to the server After you're done sending
 * discard data, call {@link org.apache.commons.net.DatagramSocketClient#close close() } to clean up properly.
 *
 * @see DiscardTCPClient
 */

public class DiscardUDPClient extends DatagramSocketClient {
    /** The default discard port. It is set to 9 according to RFC 863. */
    public static final int DEFAULT_PORT = 9;

    private final DatagramPacket sendPacket;

    public DiscardUDPClient() {
        sendPacket = new DatagramPacket(NetConstants.EMPTY_BYTE_ARRAY, 0);
    }

    /**
     * Same as <code>send(data, data.length, host. DiscardUDPClient.DEFAULT_PORT)</code>.
     *
     * @param data the buffer to send
     * @param host the target host
     * @see #send(byte[], int, InetAddress, int)
     * @throws IOException if an error occurs
     */
    public void send(final byte[] data, final InetAddress host) throws IOException {
        send(data, data.length, host, DEFAULT_PORT);
    }

    /**
     * Same as <code>send(data, length, host. DiscardUDPClient.DEFAULT_PORT)</code>.
     *
     * @param data   the buffer to send
     * @param length the length of the data in the buffer
     * @param host   the target host
     * @see #send(byte[], int, InetAddress, int)
     * @throws IOException if an error occurs
     */
    public void send(final byte[] data, final int length, final InetAddress host) throws IOException {
        send(data, length, host, DEFAULT_PORT);
    }

    /**
     * Sends the specified data to the specified server at the specified port.
     *
     * @param data   The discard data to send.
     * @param length The length of the data to send. Should be less than or equal to the length of the data byte array.
     * @param host   The address of the server.
     * @param port   The service port.
     * @throws IOException If an error occurs during the datagram send operation.
     */
    public void send(final byte[] data, final int length, final InetAddress host, final int port) throws IOException {
        sendPacket.setData(data);
        sendPacket.setLength(length);
        sendPacket.setAddress(host);
        sendPacket.setPort(port);
        _socket_.send(sendPacket);
    }

}
