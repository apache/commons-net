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

import java.net.DatagramPacket;
import java.net.InetAddress;

/***
 * An abstract class derived from TFTPPacket definiing a TFTP Request
 * packet type.  It is subclassed by the 
 * <a href="org.apache.commons.net.tftp.TFTPReadRequestPacket.html#_top_">
 * TFTPReadRequestPacket</a>  and
 * <a href="org.apache.commons.net.tftp.TFTPWriteRequestPacket.html#_top_">
 * TFTPWriteRequestPacket</a> classes.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can
 * be found in RFC 783.  But the point of these classes is to keep you
 * from having to worry about the internals.  Additionally, only very
 * few people should have to care about any of the TFTPPacket classes
 * or derived classes.  Almost all users should only be concerned with the
 * <a href="org.apache.commons.net.tftp.TFTPClient.html#_top_">TFTPClient</a> class
 * <a href="org.apache.commons.net.tftp.TFTPClient.html#receiveFile">receiveFile()</a>
 * and
 * <a href="org.apache.commons.net.tftp.TFTPClient.html#sendFile">sendFile()</a>
 * methods.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see TFTPPacket
 * @see TFTPReadRequestPacket
 * @see TFTPWriteRequestPacket
 * @see TFTPPacketException
 * @see TFTP
 ***/

public abstract class TFTPRequestPacket extends TFTPPacket
{
    /***
     * An array containing the string names of the transfer modes and indexed
     * by the transfer mode constants.
     ***/
    static final String[] _modeStrings = { "netascii", "octet" };

    /***
     * A null terminated byte array representation of the ascii names of the
     * transfer mode constants.  This is convenient for creating the TFTP
     * request packets.
     ***/
    static final byte[] _modeBytes[] = {
                                           { (byte)'n', (byte)'e', (byte)'t', (byte)'a', (byte)'s', (byte)'c',
                                             (byte)'i', (byte)'i', 0 },
                                           { (byte)'o', (byte)'c', (byte)'t', (byte)'e', (byte)'t', 0 }
                                       };

    /*** The transfer mode of the request. ***/
    int _mode;

    /*** The filename of the request. ***/
    String _filename;

    /***
     * Creates a request packet of a given type to be sent to a host at a 
     * given port with a filename and transfer mode request.
     * <p>
     * @param destination  The host to which the packet is going to be sent.
     * @param port  The port to which the packet is going to be sent.
     * @param type The type of the request (either TFTPPacket.READ_REQUEST or
     *             TFTPPacket.WRITE_REQUEST).
     * @param filename The requested filename.
     * @param mode The requested transfer mode.  This should be on of the TFTP
     *        class MODE constants (e.g., TFTP.NETASCII_MODE).
     ***/
    TFTPRequestPacket(InetAddress destination, int port,
                      int type, String filename, int mode)
    {
        super(type, destination, port);

        _filename = filename;
        _mode = mode;
    }

    /***
     * Creates a request packet of a given type based on a received
     * datagram.  Assumes the datagram is at least length 4, else an
     * ArrayIndexOutOfBoundsException may be thrown.
     * <p>
     * @param type The type of the request (either TFTPPacket.READ_REQUEST or
     *             TFTPPacket.WRITE_REQUEST).
     * @param datagram  The datagram containing the received request.
     * @throws TFTPPacketException  If the datagram isn't a valid TFTP
     *         request packet of the appropriate type.
     ***/
    TFTPRequestPacket(int type, DatagramPacket datagram)
    throws TFTPPacketException
    {
        super(type, datagram.getAddress(), datagram.getPort());

        byte[] data;
        int index, length;
        String mode;
        StringBuffer buffer;

        data = datagram.getData();

        if (getType() != data[1])
            throw new TFTPPacketException("TFTP operator code does not match type.");

        buffer = new StringBuffer();

        index = 2;
        length = datagram.getLength();

        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        _filename = buffer.toString();

        if (index >= length)
            throw new TFTPPacketException("Bad filename and mode format.");

        buffer.setLength(0);
        ++index; // need to advance beyond the end of string marker
        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        mode = buffer.toString().toLowerCase();
        length = _modeStrings.length;

        for (index = 0; index < length; index++)
        {
            if (mode.equals(_modeStrings[index]))
            {
                _mode = index;
                break;
            }
        }

        if (index >= length)
        {
            throw new TFTPPacketException("Unrecognized TFTP transfer mode: " + mode);
            // May just want to default to binary mode instead of throwing
            // exception.
            //_mode = TFTP.OCTET_MODE;
        }
    }


    /***
     * This is a method only available within the package for
     * implementing efficient datagram transport by elminating buffering.
     * It takes a datagram as an argument, and a byte buffer in which 
     * to store the raw datagram data.  Inside the method, the data
     * is set as the datagram's data and the datagram returned.
     * <p>
     * @param datagram  The datagram to create.
     * @param data The buffer to store the packet and to use in the datagram.
     * @return The datagram argument.
     ***/
    final DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data)
    {
        int fileLength, modeLength;

        fileLength = _filename.length();
        modeLength = _modeBytes[_mode].length;

        data[0] = 0;
        data[1] = (byte)_type;
        System.arraycopy(_filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(_modeBytes[_mode], 0, data, fileLength + 3,
                         modeLength);

        datagram.setAddress(_address);
        datagram.setPort(_port);
        datagram.setData(data);
        datagram.setLength(fileLength + modeLength + 4);

        return datagram;
    }

    /***
     * Creates a UDP datagram containing all the TFTP
     * request packet data in the proper format.
     * This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using
     * the <a href="org.apache.commons.net.tftp.TFTPClient.html#_top_">TFTPClient</a> 
     * class.  Under normal circumstances, you should not have a need to call
     * this method.  
     * <p>
     * @return A UDP datagram containing the TFTP request packet.
     ***/
    public final DatagramPacket newDatagram()
    {
        int fileLength, modeLength;
        byte[] data;

        fileLength = _filename.length();
        modeLength = _modeBytes[_mode].length;

        data = new byte[fileLength + modeLength + 4];
        data[0] = 0;
        data[1] = (byte)_type;
        System.arraycopy(_filename.getBytes(), 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(_modeBytes[_mode], 0, data, fileLength + 3,
                         modeLength);

        return new DatagramPacket(data, data.length, _address, _port);
    }

    /***
     * Returns the transfer mode of the request.
     * <p>
     * @return The transfer mode of the request.
     ***/
    public final int getMode()
    {
        return _mode;
    }

    /***
     * Returns the requested filename.
     * <p>
     * @return The requested filename.
     ***/
    public final String getFilename()
    {
        return _filename;
    }
}
