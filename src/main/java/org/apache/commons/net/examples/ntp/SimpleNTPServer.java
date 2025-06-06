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
package org.apache.commons.net.examples.ntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Impl;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * The SimpleNTPServer class is a UDP implementation of a server for the Network Time Protocol (NTP) version 3 as described in RFC 1305. It is a minimal NTP
 * server that doesn't actually adjust the time but only responds to NTP datagram requests with response sent back to originating host with info filled out
 * using the current clock time. To be used for debugging or testing.
 *
 * To prevent this from interfering with the actual NTP service it can be run from any local port.
 */
public class SimpleNTPServer implements Runnable {

    public static void main(final String[] args) {
        int port = NtpV3Packet.NTP_PORT;
        if (args.length != 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (final NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        final SimpleNTPServer timeServer = new SimpleNTPServer(port);
        try {
            timeServer.start();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private int port;
    private volatile boolean running;
    private boolean started;

    private DatagramSocket socket;

    /**
     * Creates SimpleNTPServer listening on default NTP port.
     */
    public SimpleNTPServer() {
        this(NtpV3Packet.NTP_PORT);
    }

    /**
     * Creates SimpleNTPServer.
     *
     * @param port the local port the server socket is bound to, or {@code zero} for a system selected free port.
     * @throws IllegalArgumentException if port number less than 0
     */
    public SimpleNTPServer(final int port) {
        if (port < 0) {
            throw new IllegalArgumentException();
        }
        this.port = port;
    }

    /**
     * Connects to server socket and listen for client connections.
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public void connect() throws IOException {
        if (socket == null) {
            socket = new DatagramSocket(port);
            // port = 0 is bound to available free port
            if (port == 0) {
                port = socket.getLocalPort();
            }
            System.out.println("Running NTP service on port " + port + "/UDP");
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Handles incoming packet. If NTP packet is client-mode then respond to that host with a NTP response packet otherwise ignore.
     *
     * @param request incoming DatagramPacket
     * @param rcvTime time packet received
     * @throws IOException if an I/O error occurs.
     */
    protected void handlePacket(final DatagramPacket request, final long rcvTime) throws IOException {
        final NtpV3Packet message = new NtpV3Impl();
        message.setDatagramPacket(request);
        System.out.printf("NTP packet from %s mode=%s%n", request.getAddress().getHostAddress(), NtpUtils.getModeName(message.getMode()));
        if (message.getMode() == NtpV3Packet.MODE_CLIENT) {
            final NtpV3Packet response = new NtpV3Impl();

            response.setStratum(1);
            response.setMode(NtpV3Packet.MODE_SERVER);
            response.setVersion(NtpV3Packet.VERSION_3);
            response.setPrecision(-20);
            response.setPoll(0);
            response.setRootDelay(62);
            response.setRootDispersion((int) (16.51 * 65.536));

            // originate time as defined in RFC-1305 (t1)
            response.setOriginateTimeStamp(message.getTransmitTimeStamp());
            // Receive Time is time request received by server (t2)
            response.setReceiveTimeStamp(TimeStamp.getNtpTime(rcvTime));
            response.setReferenceTime(response.getReceiveTimeStamp());
            response.setReferenceId(0x4C434C00); // LCL (Undisciplined Local Clock)

            // Transmit time is time reply sent by server (t3)
            response.setTransmitTime(TimeStamp.getNtpTime(System.currentTimeMillis()));

            final DatagramPacket dp = response.getDatagramPacket();
            dp.setPort(request.getPort());
            dp.setAddress(request.getAddress());
            socket.send(dp);
        }
        // otherwise if received packet is other than CLIENT mode then ignore it
    }

    /**
     * Returns state of whether time service is running.
     *
     * @return true if time service is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns state of whether time service is running.
     *
     * @return true if time service is running
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Main method to service client connections.
     */
    @Override
    public void run() {
        running = true;
        final byte[] buffer = new byte[48];
        final DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        do {
            try {
                socket.receive(request);
                final long rcvTime = System.currentTimeMillis();
                handlePacket(request, rcvTime);
            } catch (final IOException e) {
                if (running) {
                    e.printStackTrace();
                }
                // otherwise socket thrown exception during shutdown
            }
        } while (running);
    }

    /**
     * Starts time service and provide time to client connections.
     *
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public void start() throws IOException {
        if (socket == null) {
            connect();
        }
        if (!started) {
            started = true;
            new Thread(this).start();
        }
    }

    /**
     * Closes server socket and stop listening.
     */
    public void stop() {
        running = false;
        // force closing of the socket
        IOUtils.closeQuietly(socket);
        socket = null;
        started = false;
    }

}
