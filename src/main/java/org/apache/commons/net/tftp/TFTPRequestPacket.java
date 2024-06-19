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
import java.nio.charset.Charset;

/**
 * An abstract class derived from TFTPPacket definiing a TFTP Request packet type. It is subclassed by the
 * {@link org.apache.commons.net.tftp.TFTPReadRequestPacket} and {@link org.apache.commons.net.tftp.TFTPWriteRequestPacket} classes.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can be found in RFC 783. But the point of these classes is to keep you from having to
 * worry about the internals. Additionally, only very few people should have to care about any of the TFTPPacket classes or derived classes. Almost all users
 * should only be concerned with the {@link org.apache.commons.net.tftp.TFTPClient} class {@link org.apache.commons.net.tftp.TFTPClient#receiveFile
 * receiveFile()} and {@link org.apache.commons.net.tftp.TFTPClient#sendFile sendFile()} methods.
 *
 *
 * @see TFTPPacket
 * @see TFTPReadRequestPacket
 * @see TFTPWriteRequestPacket
 * @see TFTPPacketException
 * @see TFTP
 */

public abstract class TFTPRequestPacket extends TFTPPacket {
    /**
     * An array containing the string names of the transfer modes and indexed by the transfer mode constants.
     */
    static final String[] modeStrings = { "netascii", "octet" };

    /**
     * A null terminated byte array representation of the ASCII names of the transfer mode constants. This is convenient for creating the TFTP request packets.
     */
    private static final byte[] modeBytes[] = { { (byte) 'n', (byte) 'e', (byte) 't', (byte) 'a', (byte) 's', (byte) 'c', (byte) 'i', (byte) 'i', 0 },
            { (byte) 'o', (byte) 'c', (byte) 't', (byte) 'e', (byte) 't', 0 } };

    /** The transfer mode of the request. */
    private final int mode;

    /** The file name of the request. */
    private final String fileName;

    /**
     * Creates a request packet of a given type to be sent to a host at a given port with a file name and transfer mode request.
     *
     * @param destination The host to which the packet is going to be sent.
     * @param port        The port to which the packet is going to be sent.
     * @param type        The type of the request (either TFTPPacket.READ_REQUEST or TFTPPacket.WRITE_REQUEST).
     * @param fileName    The requested file name.
     * @param mode        The requested transfer mode. This should be on of the TFTP class MODE constants (e.g., TFTP.NETASCII_MODE).
     */
    TFTPRequestPacket(final InetAddress destination, final int port, final int type, final String fileName, final int mode) {
        super(type, destination, port);

        this.fileName = fileName;
        this.mode = mode;
    }

    /**
     * Creates a request packet of a given type based on a received datagram. Assumes the datagram is at least length 4, else an ArrayIndexOutOfBoundsException
     * may be thrown.
     *
     * @param type     The type of the request (either TFTPPacket.READ_REQUEST or TFTPPacket.WRITE_REQUEST).
     * @param datagram The datagram containing the received request.
     * @throws TFTPPacketException If the datagram isn't a valid TFTP request packet of the appropriate type.
     */
    TFTPRequestPacket(final int type, final DatagramPacket datagram) throws TFTPPacketException {
        super(type, datagram.getAddress(), datagram.getPort());

        final byte[] data = datagram.getData();

        if (getType() != data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }

        final StringBuilder buffer = new StringBuilder();

        int index = 2;
        int length = datagram.getLength();

        while (index < length && data[index] != 0) {
            buffer.append((char) data[index]);
            ++index;
        }

        this.fileName = buffer.toString();

        if (index >= length) {
            throw new TFTPPacketException("Bad file name and mode format.");
        }

        buffer.setLength(0);
        ++index; // need to advance beyond the end of string marker
        while (index < length && data[index] != 0) {
            buffer.append((char) data[index]);
            ++index;
        }

        final String modeString = buffer.toString().toLowerCase(java.util.Locale.ENGLISH);
        length = modeStrings.length;

        int mode = 0;
        for (index = 0; index < length; index++) {
            if (modeString.equals(modeStrings[index])) {
                mode = index;
                break;
            }
        }

        this.mode = mode;

        if (index >= length) {
            throw new TFTPPacketException("Unrecognized TFTP transfer mode: " + modeString);
            // May just want to default to binary mode instead of throwing
            // exception.
            // _mode = TFTP.OCTET_MODE;
        }
    }

    /**
     * Returns the requested file name.
     *
     * @return The requested file name.
     */
    public final String getFilename() {
        return fileName;
    }

    /**
     * Returns the transfer mode of the request.
     *
     * @return The transfer mode of the request.
     */
    public final int getMode() {
        return mode;
    }

    /**
     * Creates a UDP datagram containing all the TFTP request packet data in the proper format. This is a method exposed to the programmer in case he wants to
     * implement his own TFTP client instead of using the {@link org.apache.commons.net.tftp.TFTPClient} class. Under normal circumstances, you should not have
     * a need to call this method.
     *
     * @return A UDP datagram containing the TFTP request packet.
     */
    @Override
    public final DatagramPacket newDatagram() {
        final int fileLength;
        final int modeLength;
        final byte[] data;

        byte[] fileNameBytes = fileName.getBytes(Charset.defaultCharset());
        fileLength = fileNameBytes.length;
        modeLength = modeBytes[mode].length;

        data = new byte[fileLength + modeLength + 4];
        data[0] = 0;
        data[1] = (byte) type;
        System.arraycopy(fileNameBytes, 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(modeBytes[mode], 0, data, fileLength + 3, modeLength);

        return new DatagramPacket(data, data.length, address, port);
    }

    /**
     * This is a method only available within the package for implementing efficient datagram transport by elminating buffering. It takes a datagram as an
     * argument, and a byte buffer in which to store the raw datagram data. Inside the method, the data is set as the datagram's data and the datagram returned.
     *
     * @param datagram The datagram to create.
     * @param data     The buffer to store the packet and to use in the datagram.
     * @return The datagram argument.
     */
    @Override
    final DatagramPacket newDatagram(final DatagramPacket datagram, final byte[] data) {
        final int fileLength;
        final int modeLength;

        byte[] fileNameBytes = fileName.getBytes(Charset.defaultCharset());
        fileLength = fileNameBytes.length;
        modeLength = modeBytes[mode].length;

        data[0] = 0;
        data[1] = (byte) type;
        System.arraycopy(fileNameBytes, 0, data, 2, fileLength);
        data[fileLength + 2] = 0;
        System.arraycopy(modeBytes[mode], 0, data, fileLength + 3, modeLength);

        datagram.setAddress(address);
        datagram.setPort(port);
        datagram.setData(data);
        datagram.setLength(fileLength + modeLength + 3);

        return datagram;
    }
}
