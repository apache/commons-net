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
 * TFTPPacket is an abstract class encapsulating the functionality common
 * to the 5 types of TFTP packets.  It also provides a static factory
 * method that will create the correct TFTP packet instance from a
 * datagram.  This relieves the programmer from having to figure out what
 * kind of TFTP packet is contained in a datagram and create it himself.
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
 * @see TFTPPacketException
 * @see TFTP
 ***/

public abstract class TFTPPacket
{
    /***
     * The minimum size of a packet.  This is 4 bytes.  It is enough
     * to store the opcode and blocknumber or other required data
     * depending on the packet type.
     ***/
    static final int MIN_PACKET_SIZE = 4;

    /***
     * This is the actual TFTP spec
     * identifier and is equal to 1.   
     * Identifier returned by <a href="#getType">getType()</a>
     * indicating a read request packet.  
     ***/
    public static final int READ_REQUEST = 1;

    /***
     * This is the actual TFTP spec
     * identifier and is equal to 2.   
     * Identifier returned by <a href="#getType">getType()</a>
     * indicating a write request packet.  
     ***/
    public static final int WRITE_REQUEST = 2;

    /***
     * This is the actual TFTP spec
     * identifier and is equal to 3.
     * Identifier returned by <a href="#getType">getType()</a>
     * indicating a data packet.  
     ***/
    public static final int DATA = 3;

    /***
     * This is the actual TFTP spec
     * identifier and is equal to 4.
     * Identifier returned by <a href="#getType">getType()</a>
     * indicating an acknowledgement packet.  
     ***/
    public static final int ACKNOWLEDGEMENT = 4;

    /***
     * This is the actual TFTP spec
     * identifier and is equal to 5.
     * Identifier returned by <a href="#getType">getType()</a>
     * indicating an error packet.  
     ***/
    public static final int ERROR = 5;

    /***
     * The TFTP data packet maximum segment size in bytes.  This is 512
     * and is useful for those familiar with the TFTP protocol who want
     * to use the <a href="org.apache.commons.net.tftp.TFTP.html#_top_">TFTP</a>
     * class methods to implement their own TFTP servers or clients.
     ***/
    public static final int SEGMENT_SIZE = 512;

    /*** The type of packet. ***/
    int _type;

    /*** The port the packet came from or is going to. ***/
    int _port;

    /*** The host the packet is going to be sent or where it came from. ***/
    InetAddress _address;

    /***
     * When you receive a datagram that you expect to be a TFTP packet, you use
     * this factory method to create the proper TFTPPacket object
     * encapsulating the data contained in that datagram.  This method is the
     * only way you can instantiate a TFTPPacket derived class from a
     * datagram.
     * <p>
     * @param datagram  The datagram containing a TFTP packet.
     * @return The TFTPPacket object corresponding to the datagram.
     * @exception TFTPPacketException  If the datagram does not contain a valid
     *             TFTP packet.
     ***/
    public final static TFTPPacket newTFTPPacket(DatagramPacket datagram)
    throws TFTPPacketException
    {
        byte[] data;
        TFTPPacket packet = null;

        if (datagram.getLength() < MIN_PACKET_SIZE)
            throw new TFTPPacketException(
                "Bad packet. Datagram data length is too short.");

        data = datagram.getData();

        switch (data[1])
        {
        case READ_REQUEST:
            packet = new TFTPReadRequestPacket(datagram);
            break;
        case WRITE_REQUEST:
            packet = new TFTPWriteRequestPacket(datagram);
            break;
        case DATA:
            packet = new TFTPDataPacket(datagram);
            break;
        case ACKNOWLEDGEMENT:
            packet = new TFTPAckPacket(datagram);
            break;
        case ERROR:
            packet = new TFTPErrorPacket(datagram);
            break;
        default:
            throw new TFTPPacketException(
                "Bad packet.  Invalid TFTP operator code.");
        }

        return packet;
    }

    /***
     * This constructor is not visible outside of the package.  It is used
     * by subclasses within the package to initialize base data.
     * <p>
     * @param type The type of the packet.
     * @param address The host the packet came from or is going to be sent.
     * @param port The port the packet came from or is going to be sent.
     **/
    TFTPPacket(int type, InetAddress address, int port)
    {
        _type = type;
        _address = address;
        _port = port;
    }

    /***
     * This is an abstract method only available within the package for
     * implementing efficient datagram transport by elminating buffering.
     * It takes a datagram as an argument, and a byte buffer in which 
     * to store the raw datagram data.  Inside the method, the data
     * should be set as the datagram's data and the datagram returned.
     * <p>
     * @param datagram  The datagram to create.
     * @param data The buffer to store the packet and to use in the datagram.
     * @return The datagram argument.
     ***/
    abstract DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data);

    /***
     * Creates a UDP datagram containing all the TFTP packet
     * data in the proper format.
     * This is an abstract method, exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using
     * the <a href="org.apache.commons.net.tftp.TFTPClient.html#_top_">TFTPClient</a>
     * class.
     * Under normal circumstances, you should not have a need to call this
     * method.  
     * <p>
     * @return A UDP datagram containing the TFTP packet.
     ***/
    public abstract DatagramPacket newDatagram();

    /***
     * Returns the type of the packet.
     * <p>
     * @return The type of the packet.
     ***/
    public final int getType()
    {
        return _type;
    }

    /***
     * Returns the address of the host where the packet is going to be sent
     * or where it came from.
     * <p>
     * @return The type of the packet.
     ***/
    public final InetAddress getAddress()
    {
        return _address;
    }

    /***
     * Returns the port where the packet is going to be sent
     * or where it came from.
     * <p>
     * @return The port where the packet came from or where it is going.
     ***/
    public final int getPort()
    {
        return _port;
    }

    /*** Sets the port where the packet is going to be sent. ***/
    public final void setPort(int port)
    {
        _port = port;
    }

    /*** Sets the host address where the packet is going to be sent. ***/
    public final void setAddress(InetAddress address)
    {
        _address = address;
    }
}
