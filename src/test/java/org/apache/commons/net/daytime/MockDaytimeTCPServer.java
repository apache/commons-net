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

package org.apache.commons.net.daytime;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.MockTcpServer;

/**
 * The MockDaytimeTCPServer class is a simple TCP implementation of a server for the Daytime Protocol described in <a href="https://datatracker.ietf.org/doc/html/rfc867">RFC 867</a>.
 * <p>
 * Listens for TCP socket connections on the daytime protocol port and writes the local day time to socket {@code outputStream} as {@link String}
 * in format <code>EEEE, MMMM d, uuuu, HH:mm:ss-z</code>.
 * See the <a href="https://datatracker.ietf.org/doc/html/rfc867"> RFC-867 spec </a> for more details.
 * <p>
 * <p>
 * This implementation uses {@link MockDaytimeTCPServer#enqueue(Clock)} and {@link BlockingQueue<Clock>} to queue next {@link Clock} that will be used to obtain and
 * write daytime data into {@code clientSocket}.
 * </p>
 * <p>NOTE: this is for <b>debugging and testing purposes only</b> and not meant to be run as a reliable server.</p>
 * @see MockTcpServer
 * @see DaytimeTCPClientTest DaytimeTCPClientTest (for example usage in tests)
 */
public final class MockDaytimeTCPServer extends MockTcpServer {

    private static final DateTimeFormatter DAYTIME_DATA_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu, HH:mm:ss-z", Locale.ENGLISH);

    private final BlockingQueue<Clock> responseQueue = new LinkedBlockingQueue<>();

    /**
     * Creates new {@link MockDaytimeTCPServer} that will bind to {@link InetAddress#getLocalHost()}
     * on random port.
     *
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public MockDaytimeTCPServer() throws IOException {
    }

    /**
     * Creates new {@link MockDaytimeTCPServer} that will bind to {@link InetAddress#getLocalHost()}
     * on specified port.
     *
     * @param port the port number the server will bind to, or 0 to use a port number that is automatically allocated
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public MockDaytimeTCPServer(final int port) throws IOException {
        super(port);
    }

    /**
     * Creates new {@link MockDaytimeTCPServer} that will bind to specified {@link InetAddress} and on specified port.
     *
     * @param port the port number the server will bind to, or 0 to use a port number that is automatically allocated
     * @param serverAddress the InetAddress the server will bind to
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    public MockDaytimeTCPServer(final int port, final InetAddress serverAddress) throws IOException {
        super(port, serverAddress);
    }

    /**
     * Queues clock that will be used in next accepted {@link Socket}
     * to return Daytime data, as defined in <a href="https://datatracker.ietf.org/doc/html/rfc867">RFC 867</a> spec
     *
     * @param clock that will be used
     * @return the queued {@code clock}
     */
    public Clock enqueue(final Clock clock) {
        responseQueue.add(clock);
        return clock;
    }

    @Override
    protected void processClientSocket(final Socket clientSocket) throws Exception {
        try (final PrintWriter pw = new PrintWriter(clientSocket.getOutputStream())) {
            final Clock nextClock = Objects.requireNonNull(responseQueue.poll(5, TimeUnit.SECONDS), "Could not obtain next clock for DaytimeTCPMockServer");
            final ZonedDateTime dateTime = ZonedDateTime.now(nextClock);
            pw.write(dateTime.format(DAYTIME_DATA_FORMAT));
            pw.flush();
        }
    }

}

