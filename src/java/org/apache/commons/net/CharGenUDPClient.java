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
 * The CharGenUDPClient class is a UDP implementation of a client for the
 * character generator protocol described in RFC 864.  It can also be
 * used for Systat (RFC 866), Quote of the Day (RFC 865), and netstat
 * (port 15).  All of these protocols involve sending a datagram to the
 * appropriate port, and reading data contained in one or more reply
 * datagrams.  The chargen and quote of the day protocols only send
 * one reply datagram containing 512 bytes or less of data.  The other
 * protocols may reply with more than one datagram, in which case you
 * must wait for a timeout to determine that all reply datagrams have
 * been sent.
 * <p>
 * To use the CharGenUDPClient class, just open a local UDP port
 * with <a href="org.apache.commons.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#send"> send </a> to send the datagram that will
 * initiate the data reply.  For chargen or quote of the day, just
 * call <a href="#recieve"> receive </a>, and you're done.  For netstat and
 * systat, call receive in a while loop, and catch a SocketException and
 * InterruptedIOException to detect a timeout (don't forget to set the
 * timeout duration beforehand).  Don't forget to call
 * <a href="org.apache.commons.net.DatagramSocketClient.html#close"> close() </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see CharGenTCPClient
 ***/

public final class CharGenUDPClient extends DatagramSocketClient
{
    /*** The systat port value of 11 according to RFC 866. ***/
    public static final int SYSTAT_PORT = 11;
    /*** The netstat port value of 19. ***/
    public static final int NETSTAT_PORT = 15;
    /*** The quote of the day port value of 17 according to RFC 865. ***/
    public static final int QUOTE_OF_DAY_PORT = 17;
    /*** The character generator port value of 19 according to RFC 864. ***/
    public static final int CHARGEN_PORT = 19;
    /*** The default chargen port.  It is set to 19 according to RFC 864. ***/
    public static final int DEFAULT_PORT = 19;

    private byte[] __receiveData;
    private DatagramPacket __receivePacket;
    private DatagramPacket __sendPacket;

    /***
     * The default CharGenUDPClient constructor.  It initializes some internal
     * data structures for sending and receiving the necessary datagrams for
     * the chargen and related protocols.
     ***/
    public CharGenUDPClient()
    {
        // CharGen return packets have a maximum length of 512
        __receiveData = new byte[512];
        __receivePacket = new DatagramPacket(__receiveData, 512);
        __sendPacket = new DatagramPacket(new byte[0], 0);
    }


    /***
     * Sends the data initiation datagram.  This data in the packet is ignored
     * by the server, and merely serves to signal that the server should send
     * its reply.
     * <p>
     * @param host The address of the server.
     * @param port The port of the service.
     * @exception IOException If an error occurs while sending the datagram.
     ***/
    public void send(InetAddress host, int port) throws IOException
    {
        __sendPacket.setAddress(host);
        __sendPacket.setPort(port);
        _socket_.send(__sendPacket);
    }

    /*** Same as <code>send(host, CharGenUDPClient.DEFAULT_PORT);</code> ***/
    public void send(InetAddress host) throws IOException
    {
        send(host, DEFAULT_PORT);
    }

    /***
     * Receive the reply data from the server.  This will always be 512 bytes
     * or less.  Chargen and quote of the day only return one packet.  Netstat
     * and systat require multiple calls to receive() with timeout detection.
     * <p>
     * @return The reply data from the server.
     * @exception IOException If an error occurs while receiving the datagram.
     ***/
    public byte[] receive() throws IOException
    {
        int length;
        byte[] result;

        _socket_.receive(__receivePacket);

        result = new byte[length = __receivePacket.getLength()];
        System.arraycopy(__receiveData, 0, result, 0, length);

        return result;
    }

}

