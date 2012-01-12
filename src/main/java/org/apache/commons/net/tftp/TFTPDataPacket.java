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

package org.apache.commons.net.tftp;

import java.net.DatagramPacket;
import java.net.InetAddress;

/***
 * A final class derived from TFTPPacket definiing the TFTP Data
 * packet type.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can
 * be found in RFC 783.  But the point of these classes is to keep you
 * from having to worry about the internals.  Additionally, only very
 * few people should have to care about any of the TFTPPacket classes
 * or derived classes.  Almost all users should only be concerned with the
 * {@link org.apache.commons.net.tftp.TFTPClient} class
 * {@link org.apache.commons.net.tftp.TFTPClient#receiveFile receiveFile()}
 * and
 * {@link org.apache.commons.net.tftp.TFTPClient#sendFile sendFile()}
 * methods.
 * <p>
 * <p>
 * @see TFTPPacket
 * @see TFTPPacketException
 * @see TFTP
 ***/

public final class TFTPDataPacket extends TFTPPacket
{
    /*** The maximum number of bytes in a TFTP data packet (512) ***/
    public static final int MAX_DATA_LENGTH = 512;

    /*** The minimum number of bytes in a TFTP data packet (0) ***/
    public static final int MIN_DATA_LENGTH = 0;

    /*** The block number of the packet. ***/
    int _blockNumber;

    /*** The length of the data. ***/
    int _length;

    /*** The offset into the _data array at which the data begins. ***/
    int _offset;

    /*** The data stored in the packet. ***/
    byte[] _data;

    /***
     * Creates a data packet to be sent to a host at a given port
     * with a given block number.  The actual data to be sent is passed as
     * an array, an offset, and a length.  The offset is the offset into
     * the byte array where the data starts.  The length is the length of
     * the data.  If the length is greater than MAX_DATA_LENGTH, it is
     * truncated.
     * <p>
     * @param destination  The host to which the packet is going to be sent.
     * @param port  The port to which the packet is going to be sent.
     * @param blockNumber The block number of the data.
     * @param data The byte array containing the data.
     * @param offset The offset into the array where the data starts.
     * @param length The length of the data.
     ***/
    public TFTPDataPacket(InetAddress destination, int port, int blockNumber,
                          byte[] data, int offset, int length)
    {
        super(TFTPPacket.DATA, destination, port);

        _blockNumber = blockNumber;
        _data = data;
        _offset = offset;

        if (length > MAX_DATA_LENGTH) {
            _length = MAX_DATA_LENGTH;
        } else {
            _length = length;
        }
    }

    public TFTPDataPacket(InetAddress destination, int port, int blockNumber,
                          byte[] data)
    {
        this(destination, port, blockNumber, data, 0, data.length);
    }


    /***
     * Creates a data packet based from a received
     * datagram.  Assumes the datagram is at least length 4, else an
     * ArrayIndexOutOfBoundsException may be thrown.
     * <p>
     * @param datagram  The datagram containing the received data.
     * @throws TFTPPacketException  If the datagram isn't a valid TFTP
     *         data packet.
     ***/
    TFTPDataPacket(DatagramPacket datagram) throws TFTPPacketException
    {
        super(TFTPPacket.DATA, datagram.getAddress(), datagram.getPort());

        _data = datagram.getData();
        _offset = 4;

        if (getType() != _data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }

        _blockNumber = (((_data[2] & 0xff) << 8) | (_data[3] & 0xff));

        _length = datagram.getLength() - 4;

        if (_length > MAX_DATA_LENGTH) {
            _length = MAX_DATA_LENGTH;
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
    @Override
    DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data)
    {
        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_blockNumber & 0xffff) >> 8);
        data[3] = (byte)(_blockNumber & 0xff);

        // Doublecheck we're not the same
        if (data != _data) {
            System.arraycopy(_data, _offset, data, 4, _length);
        }

        datagram.setAddress(_address);
        datagram.setPort(_port);
        datagram.setData(data);
        datagram.setLength(_length + 4);

        return datagram;
    }

    /***
     * Creates a UDP datagram containing all the TFTP
     * data packet data in the proper format.
     * This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using
     * the {@link org.apache.commons.net.tftp.TFTPClient}
     * class.
     * Under normal circumstances, you should not have a need to call this
     * method.
     * <p>
     * @return A UDP datagram containing the TFTP data packet.
     ***/
    @Override
    public DatagramPacket newDatagram()
    {
        byte[] data;

        data = new byte[_length + 4];
        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_blockNumber & 0xffff) >> 8);
        data[3] = (byte)(_blockNumber & 0xff);

        System.arraycopy(_data, _offset, data, 4, _length);

        return new DatagramPacket(data, _length + 4, _address, _port);
    }

    /***
     * Returns the block number of the data packet.
     * <p>
     * @return The block number of the data packet.
     ***/
    public int getBlockNumber()
    {
        return _blockNumber;
    }

    /*** Sets the block number of the data packet.  ***/
    public void setBlockNumber(int blockNumber)
    {
        _blockNumber = blockNumber;
    }

    /***
     * Sets the data for the data packet.
     * <p>
     * @param data The byte array containing the data.
     * @param offset The offset into the array where the data starts.
     * @param length The length of the data.
     ***/
    public void setData(byte[] data, int offset, int length)
    {
        _data = data;
        _offset = offset;
        _length = length;

        if (length > MAX_DATA_LENGTH) {
            _length = MAX_DATA_LENGTH;
        } else {
            _length = length;
        }
    }

    /***
     * Returns the length of the data part of the data packet.
     * <p>
     * @return The length of the data part of the data packet.
     ***/
    public int getDataLength()
    {
        return _length;
    }

    /***
     * Returns the offset into the byte array where the packet data actually
     * starts.
     * <p>
     * @return The offset into the byte array where the packet data actually
     *         starts.
     ***/
    public int getDataOffset()
    {
        return _offset;
    }

    /***
     * Returns the byte array containing the packet data.
     * <p>
     * @return The byte array containing the packet data.
     ***/
    public byte[] getData()
    {
        return _data;
    }
}
