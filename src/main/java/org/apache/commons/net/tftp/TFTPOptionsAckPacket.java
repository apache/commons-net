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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A final class derived from TFTPPacket defining the TFTP OACK (Option Acknowledgment) packet type.
 * <p>
 * Details regarding this packet type can be found in RFC 2347.
 */
public final class TFTPOptionsAckPacket extends TFTPPacket {

    private final Map<String, String> options;

    /**
     * Creates an OACK packet informing which options are accepted.
     *
     * @param address The host to which the packet is going to be sent.
     * @param port The port to which the packet is going to be sent.
     * @param options The options accepted.
     */
    public TFTPOptionsAckPacket(InetAddress address, int port, Map<String, String> options) {
        super(OACK, address, port);
        this.options = new HashMap<>(options);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Creates a UDP datagram containing all the accepted options data in the proper format. This is a method exposed to the programmer in case he
     * wants to implement his own TFTP client instead of using the {@link org.apache.commons.net.tftp.TFTPClient} class. Under normal circumstances, you should
     * not have a need to call this method.
     *
     * @return A UDP datagram containing the TFTP OACK packet.
     */
    @Override
    public DatagramPacket newDatagram() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byteStream.write(0);
        byteStream.write(OACK);

        try {
            for (Map.Entry<String, String> entry : options.entrySet()) {
                byteStream.write(entry.getKey().getBytes(StandardCharsets.US_ASCII));
                byteStream.write(0);
                byteStream.write(entry.getValue().getBytes(StandardCharsets.US_ASCII));
                byteStream.write(0);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating OACK packet", e);
        }

        byte[] data = byteStream.toByteArray();
        return new DatagramPacket(data, data.length, getAddress(), getPort());
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
    DatagramPacket newDatagram(DatagramPacket datagram, byte[] data) {
        int offset = 0;
        data[offset++] = 0;
        data[offset++] = (byte) type;

        for (Map.Entry<String, String> entry : options.entrySet()) {
            byte[] key = entry.getKey().getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(key, 0, data, offset, key.length);
            offset += key.length;

            data[offset++] = 0;

            byte[] value = entry.getValue().getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(value, 0, data, offset, value.length);
            offset += value.length;

            data[offset++] = 0;
        }

        datagram.setAddress(address);
        datagram.setPort(port);
        datagram.setData(data);
        datagram.setLength(offset);

        return datagram;
    }
}
