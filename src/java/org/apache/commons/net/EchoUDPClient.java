package org.apache.commons.net;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/***
 * The EchoUDPClient class is a UDP implementation of a client for the
 * Echo protocol described in RFC 862.  To use the class,
 * just open a local UDP port
 * with <a href="org.apache.commons.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#send"> send </a> to send datagrams to the server,
 * then call <a href="#receive"> receive </a> to receive echoes.
 * After you're done echoing data, call
 * <a href="org.apache.commons.net.DatagramSocketClient.html#close"> close() </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see EchoTCPClient
 * @see DiscardUDPClient
 ***/

public final class EchoUDPClient extends DiscardUDPClient
{
    /*** The default echo port.  It is set to 7 according to RFC 862. ***/
    public static final int DEFAULT_PORT = 7;

    private DatagramPacket __receivePacket = new DatagramPacket(new byte[0], 0);

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
    public void send(byte[] data, int length, InetAddress host)
    throws IOException
    {
        send(data, length, host, DEFAULT_PORT);
    }


    /*** Same as <code> send(data, data.length, host) </code> ***/
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

