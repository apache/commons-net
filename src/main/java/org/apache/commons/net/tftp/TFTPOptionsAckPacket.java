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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A final class derived from TFTPPacket defining the TFTP OACK (Option Acknowledgment) packet type.
 * <p>
 * Details regarding this packet type can be found in RFC 2347.
 * </p>
 *
 * @since 3.12.0
 */
public final class TFTPOptionsAckPacket extends TFTPPacket {

    private final Map<String, String> options;

    /**
     * Constructs an OACK packet informing which options are accepted.
     *
     * @param address The host to which the packet is going to be sent.
     * @param port The port to which the packet is going to be sent.
     * @param options The options accepted.
     */
    public TFTPOptionsAckPacket(InetAddress address, int port, Map<String, String> options) {
        super(OACK, address, port);
        this.options = new HashMap<>(options);
    }

    /**
     * Gets the options extensions being acknowledged.
     *
     * @return The options being acknowledged.
     */
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Creates a UDP datagram containing all the accepted options data in the proper format.
     * <p>
     * This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using the {@link org.apache.commons.net.tftp.TFTPClient} class. Under normal circumstances, you should
     * not have a need to call this method.
     * </p>
     *
     * @return A UDP datagram containing the TFTP OACK packet.
     */
    @Override
    public DatagramPacket newDatagram() {
        int optionsLength = 0;
        for (Map.Entry<String, String> entry : options.entrySet()) {
            optionsLength += entry.getKey().length() + 1 + entry.getValue().length() + 1;
        }
        final byte[] data = new byte[2 + optionsLength];
        data[0] = 0;
        data[1] = (byte) type;
        writeOptionsData(data, 2);

        return new DatagramPacket(data, data.length, getAddress(), getPort());
    }

    /**
     * Creates a datagram with all the accepted options data in the proper format.
     * <p>
     * This is a method only available within the package for implementing efficient datagram transport by eliminating buffering. It takes a datagram as an
     * argument, and a byte buffer in which to store the raw datagram data. Inside the method, the data is set as the datagram's data and the datagram returned.
     * </p>
     *
     * @param datagram The datagram to create.
     * @param data     The buffer to store the packet and to use in the datagram.
     * @return The datagram argument.
     */
    @Override
    DatagramPacket newDatagram(DatagramPacket datagram, byte[] data) {
        int offset = 0;
        data[offset++] = 0;
        data[offset++] = (byte) type;
        offset = writeOptionsData(data, offset);

        datagram.setAddress(address);
        datagram.setPort(port);
        datagram.setData(data);
        datagram.setLength(offset);

        return datagram;
    }

    /**
     * This helper method will write all the options data to the provided byte array starting from the given offset.
     * All the options would get written to the data byte array in following format,
     * <pre>
     *     +--------+---+--------+---+--------+---+--------+---+
     *     |  opt1  | 0 | value1 | 0 |  optN  | 0 | valueN | 0 |
     *     +--------+---+--------+---+--------+---+--------+---+
     * </pre>
     *
     * @param data byte array to update
     * @param offset initial offset (usually the header octets)
     * @return the final offset value after writing options data to the byte array
     */
    private int writeOptionsData(byte[] data, int offset) {
        for (final Map.Entry<String, String> entry : options.entrySet()) {
            final byte[] key = entry.getKey().getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(key, 0, data, offset, key.length);
            offset += key.length;
            data[offset++] = 0;
            final byte[] value = entry.getValue().getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(value, 0, data, offset, value.length);
            offset += value.length;
            data[offset++] = 0;
        }
        return offset;
    }
}