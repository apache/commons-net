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
 * A final class derived from TFTPPacket definiing the TFTP Error
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

public final class TFTPErrorPacket extends TFTPPacket
{
    /*** The undefined error code according to RFC 783, value 0. ***/
    public static final int UNDEFINED = 0;

    /*** The file not found error code according to RFC 783, value 1. ***/
    public static final int FILE_NOT_FOUND = 1;

    /*** The access violation error code according to RFC 783, value 2. ***/
    public static final int ACCESS_VIOLATION = 2;

    /*** The disk full error code according to RFC 783, value 3. ***/
    public static final int OUT_OF_SPACE = 3;

    /***
     * The illegal TFTP operation error code according to RFC 783, value 4.
     ***/
    public static final int ILLEGAL_OPERATION = 4;

    /*** The unknown transfer id error code according to RFC 783, value 5. ***/
    public static final int UNKNOWN_TID = 5;

    /*** The file already exists error code according to RFC 783, value 6. ***/
    public static final int FILE_EXISTS = 6;

    /*** The no such user error code according to RFC 783, value 7. ***/
    public static final int NO_SUCH_USER = 7;

    /*** The error code of this packet. ***/
    int _error;

    /*** The error message of this packet. ***/
    String _message;

    /***
     * Creates an error packet to be sent to a host at a given port
     * with an error code and error message.
     * <p>
     * @param destination  The host to which the packet is going to be sent.
     * @param port  The port to which the packet is going to be sent.
     * @param error The error code of the packet.
     * @param message The error message of the packet.
     ***/
    public TFTPErrorPacket(InetAddress destination, int port,
                           int error, String message)
    {
        super(TFTPPacket.ERROR, destination, port);

        _error = error;
        _message = message;
    }

    /***
     * Creates an error packet based from a received
     * datagram.  Assumes the datagram is at least length 4, else an
     * ArrayIndexOutOfBoundsException may be thrown.
     * <p>
     * @param datagram  The datagram containing the received error.
     * @throws TFTPPacketException  If the datagram isn't a valid TFTP
     *         error packet.
     ***/
    TFTPErrorPacket(DatagramPacket datagram) throws TFTPPacketException
    {
        super(TFTPPacket.ERROR, datagram.getAddress(), datagram.getPort());
        int index, length;
        byte[] data;
        StringBuilder buffer;

        data = datagram.getData();
        length = datagram.getLength();

        if (getType() != data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }

        _error = (((data[2] & 0xff) << 8) | (data[3] & 0xff));

        if (length < 5) {
            throw new TFTPPacketException("Bad error packet. No message.");
        }

        index = 4;
        buffer = new StringBuilder();

        while (index < length && data[index] != 0)
        {
            buffer.append((char)data[index]);
            ++index;
        }

        _message = buffer.toString();
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
        int length;

        length = _message.length();

        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_error & 0xffff) >> 8);
        data[3] = (byte)(_error & 0xff);

        System.arraycopy(_message.getBytes(), 0, data, 4, length);

        data[length + 4] = 0;

        datagram.setAddress(_address);
        datagram.setPort(_port);
        datagram.setData(data);
        datagram.setLength(length + 4);

        return datagram;
    }


    /***
     * Creates a UDP datagram containing all the TFTP
     * error packet data in the proper format.
     * This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using
     * the {@link org.apache.commons.net.tftp.TFTPClient}
     * class.
     * Under normal circumstances, you should not have a need to call this
     * method.
     * <p>
     * @return A UDP datagram containing the TFTP error packet.
     ***/
    @Override
    public DatagramPacket newDatagram()
    {
        byte[] data;
        int length;

        length = _message.length();

        data = new byte[length + 5];
        data[0] = 0;
        data[1] = (byte)_type;
        data[2] = (byte)((_error & 0xffff) >> 8);
        data[3] = (byte)(_error & 0xff);

        System.arraycopy(_message.getBytes(), 0, data, 4, length);

        data[length + 4] = 0;

        return new DatagramPacket(data, data.length, _address, _port);
    }


    /***
     * Returns the error code of the packet.
     * <p>
     * @return The error code of the packet.
     ***/
    public int getError()
    {
        return _error;
    }


    /***
     * Returns the error message of the packet.
     * <p>
     * @return The error message of the packet.
     ***/
    public String getMessage()
    {
        return _message;
    }
}
