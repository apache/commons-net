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

package org.apache.commons.net.chargen;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.apache.commons.net.DatagramSocketClient;
import org.apache.commons.net.util.NetConstants;

/**
 * The CharGenUDPClient class is a UDP implementation of a client for the character generator protocol described in RFC 864. It can also be used for Systat (RFC
 * 866), Quote of the Day (RFC 865), and netstat (port 15). All of these protocols involve sending a datagram to the appropriate port, and reading data
 * contained in one or more reply datagrams. The chargen and quote of the day protocols only send one reply datagram containing 512 bytes or fewer. The
 * other protocols may reply with more than one datagram, in which case you must wait for a timeout to determine that all reply datagrams have been sent.
 * <p>
 * To use the CharGenUDPClient class, just open a local UDP port with {@link org.apache.commons.net.DatagramSocketClient#open open } and call {@link #send send
 * } to send the datagram that will initiate the data reply. For chargen or quote of the day, just call {@link #receive receive }, and you're done. For netstat
 * and systat, call receive in a while loop, and catch a SocketException and InterruptedIOException to detect a timeout (don't forget to set the timeout
 * duration beforehand). Don't forget to call {@link org.apache.commons.net.DatagramSocketClient#close close() } to clean up properly.
 *
 * @see CharGenTCPClient
 */

public final class CharGenUDPClient extends DatagramSocketClient {
    /** The systat port value of 11 according to RFC 866. */
    public static final int SYSTAT_PORT = 11;
    /** The netstat port value of 19. */
    public static final int NETSTAT_PORT = 15;
    /** The quote of the day port value of 17 according to RFC 865. */
    public static final int QUOTE_OF_DAY_PORT = 17;
    /** The character generator port value of 19 according to RFC 864. */
    public static final int CHARGEN_PORT = 19;
    /** The default chargen port. It is set to 19 according to RFC 864. */
    public static final int DEFAULT_PORT = 19;

    private final byte[] receiveData;
    private final DatagramPacket receivePacket;
    private final DatagramPacket sendPacket;

    /**
     * The default CharGenUDPClient constructor. It initializes some internal data structures for sending and receiving the necessary datagrams for the chargen
     * and related protocols.
     */
    public CharGenUDPClient() {
        // CharGen return packets have a maximum length of 512
        receiveData = new byte[512];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        sendPacket = new DatagramPacket(NetConstants.EMPTY_BYTE_ARRAY, 0);
    }

    /**
     * Receive the reply data from the server. This will always be 512 bytes or fewer. Chargen and quote of the day only return one packet. Netstat and systat
     * require multiple calls to receive() with timeout detection.
     *
     * @return The reply data from the server.
     * @throws IOException If an error occurs while receiving the datagram.
     */
    public byte[] receive() throws IOException {
        final int length;
        final byte[] result;

        _socket_.receive(receivePacket);

        result = new byte[length = receivePacket.getLength()];
        System.arraycopy(receiveData, 0, result, 0, length);

        return result;
    }

    /**
     * Same as <code>send(host, CharGenUDPClient.DEFAULT_PORT);</code>
     *
     * @param host the destination host
     * @throws IOException on error
     */
    public void send(final InetAddress host) throws IOException {
        send(host, DEFAULT_PORT);
    }

    /**
     * Sends the data initiation datagram. This data in the packet is ignored by the server, and merely serves to signal that the server should send its reply.
     *
     * @param host The address of the server.
     * @param port The port of the service.
     * @throws IOException If an error occurs while sending the datagram.
     */
    public void send(final InetAddress host, final int port) throws IOException {
        sendPacket.setAddress(host);
        sendPacket.setPort(port);
        _socket_.send(sendPacket);
    }

}
