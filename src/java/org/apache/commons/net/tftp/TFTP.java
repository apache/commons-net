package org.apache.commons.net.tftp;

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
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import org.apache.commons.net.DatagramSocketClient;

/***
 * The TFTP class exposes a set of methods to allow you to deal with the TFTP
 * protocol directly, in case you want to write your own TFTP client or
 * server.  However, almost every user should only be concerend with
 * the <a href="org.apache.commons.net.DatagramSocketClient.html#open"> open() </a>,
 * and <a href="org.apache.commons.net.DatagramSocketClient.html#close"> close() </a>,
 * methods. Additionally,the a
 * <a href="org.apache.commons.net.DatagramSocketClient.html#setDefaultTimeout">
 * setDefaultTimeout() </a> method may be of importance for performance tuning.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can
 * be found in RFC 783.  But the point of these classes is to keep you
 * from having to worry about the internals.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see org.apache.commons.net.DatagramSocketClient
 * @see TFTPPacket
 * @see TFTPPacketException
 * @see TFTPClient
 ***/

public class TFTP extends DatagramSocketClient
{
    /***
     * The ascii transfer mode.  Its value is 0 and equivalent to NETASCII_MODE
     ***/
    public static final int ASCII_MODE = 0;

    /***
     * The netascii transfer mode.  Its value is 0.
     ***/
    public static final int NETASCII_MODE = 0;

    /***
     * The binary transfer mode.  Its value is 1 and equivalent to OCTET_MODE.
     ***/
    public static final int BINARY_MODE = 1;

    /***
     * The image transfer mode.  Its value is 1 and equivalent to OCTET_MODE.
     ***/
    public static final int IMAGE_MODE = 1;

    /***
     * The octet transfer mode.  Its value is 1.
     ***/
    public static final int OCTET_MODE = 1;

    /***
     * The default number of milliseconds to wait to receive a datagram
     * before timing out.  The default is 5000 milliseconds (5 seconds).
     ***/
    public static final int DEFAULT_TIMEOUT = 5000;

    /***
     * The default TFTP port according to RFC 783 is 69.
     ***/
    public static final int DEFAULT_PORT = 69;

    /***
     * The size to use for TFTP packet buffers.  Its 4 plus the 
     * TFTPPacket.SEGMENT_SIZE, i.e. 516.
     ***/
    static final int PACKET_SIZE = TFTPPacket.SEGMENT_SIZE + 4;

    /*** A buffer used to accelerate receives in bufferedReceive() ***/
    private byte[] __receiveBuffer;

    /*** A datagram used to minimize memory allocation in bufferedReceive() ***/
    private DatagramPacket __receiveDatagram;

    /*** A datagram used to minimize memory allocation in bufferedSend() ***/
    private DatagramPacket __sendDatagram;

    /***
     * A buffer used to accelerate sends in bufferedSend().
     * It is left package visible so that TFTPClient may be slightly more
     * efficient during file sends.  It saves the creation of an
     * additional buffer and prevents a buffer copy in _newDataPcket().
     ***/
    byte[] _sendBuffer;


    /***
     * Returns the TFTP string representation of a TFTP transfer mode.
     * Will throw an ArrayIndexOutOfBoundsException if an invalid transfer
     * mode is specified.
     * <p>
     * @param mode  The TFTP transfer mode.  One of the MODE constants.
     * @return  The TFTP string representation of the TFTP transfer mode.
     ***/
    public static final String getModeName(int mode)
    {
        return TFTPRequestPacket._modeStrings[mode];
    }

    /***
     * Creates a TFTP instance with a default timeout of DEFAULT_TIMEOUT,
     * a null socket, and buffered operations disabled.
     ***/
    public TFTP()
    {
        setDefaultTimeout(DEFAULT_TIMEOUT);
        __receiveBuffer = null;
        __receiveDatagram = null;
    }

    /***
     * This method synchronizes a connection by discarding all packets that
     * may be in the local socket buffer.  This method need only be called
     * when you implement your own TFTP client or server.
     * <p>
     * @exception IOException if an I/O error occurs.
     ***/
    public final void discardPackets() throws IOException
    {
        int to;
        DatagramPacket datagram;

        datagram = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

        to = getSoTimeout();
        setSoTimeout(1);

        try
        {
            while (true)
                _socket_.receive(datagram);
        }
        catch (SocketException e)
        {
            // Do nothing.  We timed out so we hope we're caught up.
        }
        catch (InterruptedIOException e)
        {
            // Do nothing.  We timed out so we hope we're caught up.
        }

        setSoTimeout(to);
    }


    /***
     * This is a special method to perform a more efficient packet receive.
     * It should only be used after calling
     * <a href="#beginBufferedOps"> beginBufferedOps() </a>.  beginBufferedOps()
     * initializes a set of buffers used internally that prevent the new
     * allocation of a DatagramPacket and byte array for each send and receive.
     * To use these buffers you must call the bufferedReceive() and 
     * bufferedSend() methods instead of send() and receive().  You must
     * also be certain that you don't manipulate the resulting packet in
     * such a way that it interferes with future buffered operations.  
     * For example, a TFTPDataPacket received with bufferedReceive() will
     * have a reference to the internal byte buffer.  You must finish using
     * this data before calling bufferedReceive() again, or else the data
     * will be overwritten by the the call.
     * <p>
     * @return The TFTPPacket received.
     * @exception InterruptedIOException  If a socket timeout occurs.  The
     *       Java documentation claims an InterruptedIOException is thrown
     *       on a DatagramSocket timeout, but in practice we find a
     *       SocketException is thrown.  You should catch both to be safe.
     * @exception SocketException  If a socket timeout occurs.  The
     *       Java documentation claims an InterruptedIOException is thrown
     *       on a DatagramSocket timeout, but in practice we find a
     *       SocketException is thrown.  You should catch both to be safe.
     * @exception IOException  If some other I/O error occurs.
     * @exception TFTPPacketException If an invalid TFTP packet is received.
     ***/
    public final TFTPPacket bufferedReceive() throws IOException,
                InterruptedIOException, SocketException, TFTPPacketException
    {
        __receiveDatagram.setData(__receiveBuffer);
        __receiveDatagram.setLength(__receiveBuffer.length);
        _socket_.receive(__receiveDatagram);

        return TFTPPacket.newTFTPPacket(__receiveDatagram);
    }

    /***
     * This is a special method to perform a more efficient packet send.
     * It should only be used after calling
     * <a href="#beginBufferedOps"> beginBufferedOps() </a>.  beginBufferedOps()
     * initializes a set of buffers used internally that prevent the new
     * allocation of a DatagramPacket and byte array for each send and receive.
     * To use these buffers you must call the bufferedReceive() and 
     * bufferedSend() methods instead of send() and receive().  You must
     * also be certain that you don't manipulate the resulting packet in
     * such a way that it interferes with future buffered operations.  
     * For example, a TFTPDataPacket received with bufferedReceive() will
     * have a reference to the internal byte buffer.  You must finish using
     * this data before calling bufferedReceive() again, or else the data
     * will be overwritten by the the call.
     * <p>
     * @param TFTPPacket  The TFTP packet to send.
     * @exception IOException  If some  I/O error occurs.
     ***/
    public final void bufferedSend(TFTPPacket packet) throws IOException
    {
        _socket_.send(packet._newDatagram(__sendDatagram, _sendBuffer));
    }


    /***
     * Initializes the internal buffers. Buffers are used by
     * <a href="#bufferedSend"> bufferedSend() </a> and 
     * <a href="#bufferedReceive"> bufferedReceive() </a>.  This
     * method must be called before calling either one of those two
     * methods.  When you finish using buffered operations, you must
     * call <a href="#endBufferedOps"> endBufferedOps() </a>.
     ***/
    public final void beginBufferedOps()
    {
        __receiveBuffer = new byte[PACKET_SIZE];
        __receiveDatagram =
            new DatagramPacket(__receiveBuffer, __receiveBuffer.length);
        _sendBuffer = new byte[PACKET_SIZE];
        __sendDatagram =
            new DatagramPacket(_sendBuffer, _sendBuffer.length);
    }

    /***
     * Releases the resources used to perform buffered sends and receives.
     ***/
    public final void endBufferedOps()
    {
        __receiveBuffer = null;
        __receiveDatagram = null;
        _sendBuffer = null;
        __sendDatagram = null;
    }


    /***
     * Sends a TFTP packet to its destination.
     * <p>
     * @param TFTPPacket  The TFTP packet to send.
     * @exception IOException  If some  I/O error occurs.
     ***/
    public final void send(TFTPPacket packet) throws IOException
    {
        _socket_.send(packet.newDatagram());
    }


    /***
     * Receives a TFTPPacket.
     * <p>
     * @return The TFTPPacket received.
     * @exception InterruptedIOException  If a socket timeout occurs.  The
     *       Java documentation claims an InterruptedIOException is thrown
     *       on a DatagramSocket timeout, but in practice we find a
     *       SocketException is thrown.  You should catch both to be safe.
     * @exception SocketException  If a socket timeout occurs.  The
     *       Java documentation claims an InterruptedIOException is thrown
     *       on a DatagramSocket timeout, but in practice we find a
     *       SocketException is thrown.  You should catch both to be safe.
     * @exception IOException  If some other I/O error occurs.
     * @exception TFTPPacketException If an invalid TFTP packet is received.
     ***/
    public final TFTPPacket receive() throws IOException, InterruptedIOException,
                SocketException, TFTPPacketException
    {
        DatagramPacket packet;

        packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

        _socket_.receive(packet);

        return TFTPPacket.newTFTPPacket(packet);
    }


}
