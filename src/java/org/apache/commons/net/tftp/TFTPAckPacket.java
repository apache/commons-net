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
 * A final class derived from TFTPPacket definiing the TFTP Acknowledgement
 * packet type.
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
 * @see TFTPPacketException
 * @see TFTP
 ***/

public final class TFTPAckPacket extends TFTPPacket
{
    /*** The block number being acknowledged by the packet. ***/
    int _blockNumber;

    /***
     * Creates an acknowledgment packet to be sent to a host at a given port
     * acknowledging receipt of a block.
     * <p>
     * @param destination  The host to which the packet is going to be sent.
     * @param port  The port to which the packet is going to be sent.
     * @param blockNumber  The block number being acknowledged.
     ***/
    public TFTPAckPacket(InetAddress destination, int port, int blockNumber)
    {
        super(TFTPPacket.ACKNOWLEDGEMENT, destination, port);
        _blockNumber = blockNumber;
    }

    /***
     * Creates an acknowledgement packet based from a received
     * datagram.  Assumes the datagram is at least length 4, else an
     * ArrayIndexOutOfBoundsException may be thrown.
     * <p>
     * @param datagram  The datagram containing the received acknowledgement.
     * @throws TFTPPacketException  If the datagram isn't a valid TFTP
     *         acknowledgement packet.
     ***/
    TFTPAckPacket(DatagramPacket datagram) throws TFTPPacketException
    {
        super(TFTPPacket.ACKNOWLEDGEMENT, datagram.getAddress(),
              datagram.getPort());
        byte[] data;

        data = datagram.getData();

        if (getType() != data[1])
            throw new TFTPPacketException("TFTP operator code does not match type.");

        _blockNumber = (((data[2] & 0xff) << 8) | (data[3] & 0xff));
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
    DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data)
    {
        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_blockNumber & 0xffff) >> 8);
        data[3] = (byte)(_blockNumber & 0xff);

        datagram.setAddress(_address);
        datagram.setPort(_port);
        datagram.setData(data);
        datagram.setLength(4);

        return datagram;
    }


    /***
     * Creates a UDP datagram containing all the TFTP
     * acknowledgement packet data in the proper format.
     * This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using
     * the <a href="org.apache.commons.net.tftp.TFTPClient.html#_top_">TFTPClient</a>
     * class.  Under normal circumstances, you should not have a need to call this
     * method.  
     * <p>
     * @return A UDP datagram containing the TFTP acknowledgement packet.
     ***/
    public DatagramPacket newDatagram()
    {
        byte[] data;

        data = new byte[4];
        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_blockNumber & 0xffff) >> 8);
        data[3] = (byte)(_blockNumber & 0xff);

        return new DatagramPacket(data, data.length, _address, _port);
    }


    /***
     * Returns the block number of the acknowledgement.
     * <p>
     * @return The block number of the acknowledgement.
     ***/
    public int getBlockNumber()
    {
        return _blockNumber;
    }


    /*** Sets the block number of the acknowledgement.  ***/
    public void setBlockNumber(int blockNumber)
    {
        _blockNumber = blockNumber;
    }
}

