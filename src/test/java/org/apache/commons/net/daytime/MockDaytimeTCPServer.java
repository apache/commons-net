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

import org.apache.commons.net.MockTcpServer;

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

public final class MockDaytimeTCPServer extends MockTcpServer {

    private static final DateTimeFormatter DAYTIME_DATA_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu, HH:mm:ss-z", Locale.ENGLISH);

    private final BlockingQueue<Clock> responseQueue = new LinkedBlockingQueue<>();

    public MockDaytimeTCPServer() throws IOException {
    }

    public MockDaytimeTCPServer(int port) throws IOException {
        super(port);
    }

    public MockDaytimeTCPServer(int port, InetAddress serverAddress) throws IOException {
        super(port, serverAddress);
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

    public Clock enqueue(final Clock clock) {
        responseQueue.add(clock);
        return clock;
    }

}
