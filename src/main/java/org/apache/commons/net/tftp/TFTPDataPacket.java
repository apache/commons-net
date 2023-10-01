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

/**
 * A final class derived from TFTPPacket defining the TFTP Data packet type.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can be found in RFC 783. But the point of these classes is to keep you from having to
 * worry about the internals. Additionally, only very few people should have to care about any of the TFTPPacket classes or derived classes. Almost all users
 * should only be concerned with the {@link org.apache.commons.net.tftp.TFTPClient} class {@link org.apache.commons.net.tftp.TFTPClient#receiveFile
 * receiveFile()} and {@link org.apache.commons.net.tftp.TFTPClient#sendFile sendFile()} methods.
 *
 *
 * @see TFTPPacket
 * @see TFTPPacketException
 * @see TFTP
 */

public final class TFTPDataPacket extends TFTPPacket {
    /** The maximum number of bytes in a TFTP data packet (512) */
    public static final int MAX_DATA_LENGTH = 512;

    /** The minimum number of bytes in a TFTP data packet (0) */
    public static final int MIN_DATA_LENGTH = 0;

    /** The block number of the packet. */
    int blockNumber;

    /** The length of the data. */
    private int length;

    /** The offset into the _data array at which the data begins. */
    private int offset;

    /** The data stored in the packet. */
    private byte[] data;

    /**
     * Creates a data packet based from a received datagram. Assumes the datagram is at least length 4, else an ArrayIndexOutOfBoundsException may be thrown.
     *
     * @param datagram The datagram containing the received data.
     * @throws TFTPPacketException If the datagram isn't a valid TFTP data packet.
     */
    TFTPDataPacket(final DatagramPacket datagram) throws TFTPPacketException {
        super(TFTPPacket.DATA, datagram.getAddress(), datagram.getPort());

        this.data = datagram.getData();
        this.offset = 4;

        if (getType() != this.data[1]) {
            throw new TFTPPacketException("TFTP operator code does not match type.");
        }

        this.blockNumber = (this.data[2] & 0xff) << 8 | this.data[3] & 0xff;

        this.length = datagram.getLength() - 4;

        if (this.length > MAX_DATA_LENGTH) {
            this.length = MAX_DATA_LENGTH;
        }
    }

    public TFTPDataPacket(final InetAddress destination, final int port, final int blockNumber, final byte[] data) {
        this(destination, port, blockNumber, data, 0, data.length);
    }

    /**
     * Creates a data packet to be sent to a host at a given port with a given block number. The actual data to be sent is passed as an array, an offset, and a
     * length. The offset is the offset into the byte array where the data starts. The length is the length of the data. If the length is greater than
     * MAX_DATA_LENGTH, it is truncated.
     *
     * @param destination The host to which the packet is going to be sent.
     * @param port        The port to which the packet is going to be sent.
     * @param blockNumber The block number of the data.
     * @param data        The byte array containing the data.
     * @param offset      The offset into the array where the data starts.
     * @param length      The length of the data.
     */
    public TFTPDataPacket(final InetAddress destination, final int port, final int blockNumber, final byte[] data, final int offset, final int length) {
        super(TFTPPacket.DATA, destination, port);

        this.blockNumber = blockNumber;
        this.data = data;
        this.offset = offset;

        this.length = Math.min(length, MAX_DATA_LENGTH);
    }

    /**
     * Returns the block number of the data packet.
     *
     * @return The block number of the data packet.
     */
    public int getBlockNumber() {
        return blockNumber;
    }

    /**
     * Returns the byte array containing the packet data.
     *
     * @return The byte array containing the packet data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the length of the data part of the data packet.
     *
     * @return The length of the data part of the data packet.
     */
    public int getDataLength() {
        return length;
    }

    /**
     * Returns the offset into the byte array where the packet data actually starts.
     *
     * @return The offset into the byte array where the packet data actually starts.
     */
    public int getDataOffset() {
        return offset;
    }

    /**
     * Creates a UDP datagram containing all the TFTP data packet data in the proper format. This is a method exposed to the programmer in case he wants to
     * implement his own TFTP client instead of using the {@link org.apache.commons.net.tftp.TFTPClient} class. Under normal circumstances, you should not have
     * a need to call this method.
     *
     * @return A UDP datagram containing the TFTP data packet.
     */
    @Override
    public DatagramPacket newDatagram() {
        final byte[] data;

        data = new byte[length + 4];
        data[0] = 0;
        data[1] = (byte) type;
        data[2] = (byte) ((blockNumber & 0xffff) >> 8);
        data[3] = (byte) (blockNumber & 0xff);

        System.arraycopy(this.data, offset, data, 4, length);

        return new DatagramPacket(data, length + 4, address, port);
    }

    /**
     * This is a method only available within the package for implementing efficient datagram transport by eliminating buffering. It takes a datagram as an
     * argument, and a byte buffer in which to store the raw datagram data. Inside the method, the data is set as the datagram's data and the datagram returned.
     *
     * @param datagram The datagram to create.
     * @param data     The buffer to store the packet and to use in the datagram.
     * @return The datagram argument.
     */
    @Override
    DatagramPacket newDatagram(final DatagramPacket datagram, final byte[] data) {
        data[0] = 0;
        data[1] = (byte) type;
        data[2] = (byte) ((blockNumber & 0xffff) >> 8);
        data[3] = (byte) (blockNumber & 0xff);

        // Doublecheck we're not the same
        if (data != this.data) {
            System.arraycopy(this.data, offset, data, 4, length);
        }

        datagram.setAddress(address);
        datagram.setPort(port);
        datagram.setData(data);
        datagram.setLength(length + 4);

        return datagram;
    }

    /**
     * Sets the block number of the data packet.
     *
     * @param blockNumber the number to set
     */
    public void setBlockNumber(final int blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Sets the data for the data packet.
     *
     * @param data   The byte array containing the data.
     * @param offset The offset into the array where the data starts.
     * @param length The length of the data.
     */
    public void setData(final byte[] data, final int offset, final int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;

        this.length = Math.min(length, MAX_DATA_LENGTH);
    }

    /**
     * For debugging
     *
     * @since 3.6
     */
    @Override
    public String toString() {
        return super.toString() + " DATA " + blockNumber + " " + length;
    }
}
