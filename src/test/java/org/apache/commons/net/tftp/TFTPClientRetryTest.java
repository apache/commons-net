/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.tftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests retransmission of uploads by {@link TFTPClient}.
 */
@Timeout(15)
class TFTPClientRetryTest {

    @ParameterizedTest
    @CsvSource({
        "0, 0, false",
        "17, 0, false",
        "512, 0, false",
        "513, 0, false",
        "0, 1, false",
        "17, 1, false",
        "512, 1, false",
        "512, 2, false",
        "513, 1, false",
        "513, 2, false",
        "17, 0, true"
    })
    void testRetrySendFile(final int dataLength, final int dropDataBlock, final boolean dropWriteAck) throws Exception {
        final byte[] payload = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            payload[i] = (byte) (i % 256);
        }

        final InetAddress host = InetAddress.getByName("127.0.0.1");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try (TFTP control = new TFTP();
                TFTP transfer = new TFTP();
                TFTPClient client = new TFTPClient()) {
            control.open(0, host);
            transfer.open(0, host);
            client.open(0, host);
            control.setSoTimeout(Duration.ofSeconds(5));
            transfer.setSoTimeout(Duration.ofSeconds(5));
            client.setSoTimeout(Duration.ofMillis(250));
            client.setMaxTimeouts(3);

            final Future<byte[]> serverFuture = executor.submit(() -> {
                final TFTPPacket request = control.receive();
                assertEquals(TFTPPacket.WRITE_REQUEST, request.getType());

                if (dropWriteAck) {
                    // Wait for the retried identical WRQ
                    final TFTPPacket secondRequest = control.receive();
                    assertEquals(TFTPPacket.WRITE_REQUEST, secondRequest.getType());
                    assertEquals(request.getPort(), secondRequest.getPort());
                    assertEquals(request.getAddress(), secondRequest.getAddress());
                    assertArrayEquals(request.newDatagram().getData(), secondRequest.newDatagram().getData());
                }

                final InetAddress clientAddress = request.getAddress();
                final int clientPort = request.getPort();

                final TFTPPacket ack0 = new TFTPAckPacket(clientAddress, clientPort, 0);
                transfer.send(ack0);

                final ByteArrayOutputStream receivedData = new ByteArrayOutputStream();
                int expectedBlock = 1;
                while (true) {
                    final TFTPPacket packet = transfer.receive();
                    assertEquals(TFTPPacket.DATA, packet.getType());
                    final TFTPDataPacket dataPacket = (TFTPDataPacket) packet;
                    final int block = dataPacket.getBlockNumber();
                    assertEquals(expectedBlock, block);
                    final byte[] currentData = new byte[dataPacket.getDataLength()];
                    System.arraycopy(dataPacket.getData(), dataPacket.getDataOffset(), currentData, 0, dataPacket.getDataLength());

                    if (block == dropDataBlock) {
                        // Withhold ACK, expect retry duplicate
                        final TFTPPacket duplicatePacket = transfer.receive();
                        assertEquals(TFTPPacket.DATA, duplicatePacket.getType());
                        final TFTPDataPacket duplicateDataPacket = (TFTPDataPacket) duplicatePacket;
                        assertEquals(block, duplicateDataPacket.getBlockNumber());
                        assertArrayEquals(currentData,
                                Arrays.copyOfRange(duplicateDataPacket.getData(),
                                        duplicateDataPacket.getDataOffset(),
                                        duplicateDataPacket.getDataOffset() + duplicateDataPacket.getDataLength()));
                    }

                    receivedData.write(currentData, 0, currentData.length);
                    final TFTPPacket ack = new TFTPAckPacket(clientAddress, clientPort, block);
                    transfer.send(ack);

                    expectedBlock++;
                    if (currentData.length < TFTPPacket.SEGMENT_SIZE) {
                        break;
                    }
                }
                return receivedData.toByteArray();
            });

            try (ByteArrayInputStream input = new ByteArrayInputStream(payload)) {
                client.sendFile("test.bin", TFTP.BINARY_MODE, input, host, control.getLocalPort());
            }

            final byte[] receivedPayload = serverFuture.get(10, TimeUnit.SECONDS);
            assertArrayEquals(payload, receivedPayload);
            assertEquals(payload.length, client.getTotalBytesSent());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void testRetryPreservesDataAfterWrongTidReply() throws Exception {
        final byte[] expectedPayload = new byte[17];
        for (int i = 0; i < expectedPayload.length; i++) {
            expectedPayload[i] = (byte) (i * 7 + 3);
        }

        final InetAddress host = InetAddress.getByName("127.0.0.1");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try (TFTP control = new TFTP();
                TFTP transfer = new TFTP();
                TFTP stray = new TFTP();
                TFTPClient client = new TFTPClient()) {
            control.open(0, host);
            transfer.open(0, host);
            stray.open(0, host);
            client.open(0, host);
            control.setSoTimeout(Duration.ofSeconds(5));
            transfer.setSoTimeout(Duration.ofSeconds(5));
            stray.setSoTimeout(Duration.ofSeconds(5));
            client.setSoTimeout(Duration.ofMillis(250));
            client.setMaxTimeouts(3);

            final Future<byte[]> serverFuture = executor.submit(() -> {
                final TFTPPacket request = control.receive();
                assertEquals(TFTPPacket.WRITE_REQUEST, request.getType());

                final InetAddress clientAddress = request.getAddress();
                final int clientPort = request.getPort();

                final TFTPPacket ack0 = new TFTPAckPacket(clientAddress, clientPort, 0);
                transfer.send(ack0);

                final TFTPPacket data1 = transfer.receive();
                assertEquals(TFTPPacket.DATA, data1.getType());
                final TFTPDataPacket initialDataPacket = (TFTPDataPacket) data1;
                assertEquals(1, initialDataPacket.getBlockNumber());
                final byte[] initialPayload = new byte[initialDataPacket.getDataLength()];
                System.arraycopy(initialDataPacket.getData(), initialDataPacket.getDataOffset(), initialPayload, 0, initialDataPacket.getDataLength());
                assertArrayEquals(expectedPayload, initialPayload);

                final TFTPPacket ackFromStray = new TFTPAckPacket(clientAddress, clientPort, 1);
                stray.send(ackFromStray);

                final TFTPPacket errorReply = stray.receive();
                assertEquals(TFTPPacket.ERROR, errorReply.getType());
                assertEquals(TFTPErrorPacket.UNKNOWN_TID, ((TFTPErrorPacket) errorReply).getError());

                final TFTPPacket retriedData = transfer.receive();
                assertEquals(TFTPPacket.DATA, retriedData.getType());
                final TFTPDataPacket retriedDataPacket = (TFTPDataPacket) retriedData;
                assertEquals(1, retriedDataPacket.getBlockNumber());
                final byte[] retriedPayload = new byte[retriedDataPacket.getDataLength()];
                System.arraycopy(retriedDataPacket.getData(), retriedDataPacket.getDataOffset(), retriedPayload, 0, retriedDataPacket.getDataLength());

                final TFTPPacket ack1 = new TFTPAckPacket(clientAddress, clientPort, 1);
                transfer.send(ack1);

                return retriedPayload;
            });

            try (ByteArrayInputStream input = new ByteArrayInputStream(expectedPayload)) {
                client.sendFile("corrupt.bin", TFTP.BINARY_MODE, input, host, control.getLocalPort());
            }

            final byte[] retriedPayload = serverFuture.get(10, TimeUnit.SECONDS);
            assertArrayEquals(expectedPayload, retriedPayload);
            assertEquals(expectedPayload.length, client.getTotalBytesSent());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
