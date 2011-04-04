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

package org.apache.commons.net.echo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.apache.commons.net.discard.DiscardUDPClient;

/***
 * The EchoUDPClient class is a UDP implementation of a client for the
 * Echo protocol described in RFC 862.  To use the class,
 * just open a local UDP port
 * with {@link org.apache.commons.net.DatagramSocketClient#open  open }
 * and call {@link #send  send } to send datagrams to the server,
 * then call {@link #receive  receive } to receive echoes.
 * After you're done echoing data, call
 * {@link org.apache.commons.net.DatagramSocketClient#close  close() }
 * to clean up properly.
 * <p>
 * <p>
 * @see EchoTCPClient
 * @see DiscardUDPClient
 ***/

public final class EchoUDPClient extends DiscardUDPClient
{
    /*** The default echo port.  It is set to 7 according to RFC 862. ***/
    public static final int DEFAULT_PORT = 7;

    private final DatagramPacket __receivePacket = new DatagramPacket(new byte[0], 0);

    /***
     * Sends the specified data to the specified server at the default echo
     * port.
     * <p>
     * @param data  The echo data to send.
     * @param length  The length of the data to send.  Should be less than
     *    or equal to the length of the data byte array.
     * @param host  The address of the server.
     * @exception IOException If an error occurs during the datagram send
     *     operation.
     ***/
    @Override
    public void send(byte[] data, int length, InetAddress host)
    throws IOException
    {
        send(data, length, host, DEFAULT_PORT);
    }


    /*** Same as <code> send(data, data.length, host) </code> ***/
    @Override
    public void send(byte[] data, InetAddress host) throws IOException
    {
        send(data, data.length, host, DEFAULT_PORT);
    }


    /***
     * Receives echoed data and returns its length.  The data may be divided
     * up among multiple datagrams, requiring multiple calls to receive.
     * Also, the UDP packets will not necessarily arrive in the same order
     * they were sent.
     * <p>
     * @return  Length of actual data received.
     * @exception IOException If an error occurs while receiving the data.
     ***/
    public int receive(byte[] data, int length) throws IOException
    {
        __receivePacket.setData(data);
        __receivePacket.setLength(length);
        _socket_.receive(__receivePacket);
        return __receivePacket.getLength();
    }

    /*** Same as <code> receive(data, data.length)</code> ***/
    public int receive(byte[] data) throws IOException
    {
        return receive(data, data.length);
    }

}

