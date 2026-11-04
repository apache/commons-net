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

package org.apache.commons.net.ftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FTPClient#login(String, char[])} (NET-746): the password is sent as a {@code char} array, never materialized into a {@link String}, and the
 * array is cleared once the password has been transmitted.
 */
class FTPClientLoginCharPasswordTest {

    /**
     * A minimal scripted FTP server running on a local port.
     */
    private static final class FakeFtpServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final StringBuffer received = new StringBuffer();
        private final String userReply;
        private final String passwordReply;

        FakeFtpServer(final String userReply, final String passwordReply) throws IOException {
            this.serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            this.userReply = userReply;
            this.passwordReply = passwordReply;
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        String getReceived() {
            synchronized (received) {
                return received.toString();
            }
        }

        void start() {
            final Thread thread = new Thread(this::run, "fake-ftp-server");
            thread.setDaemon(true);
            thread.start();
        }

        private void run() {
            try (Socket socket = serverSocket.accept(); OutputStream out = socket.getOutputStream()) {
                out.write("220 fake\r\n".getBytes(StandardCharsets.ISO_8859_1));
                out.flush();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        synchronized (received) {
                            received.append(line).append("\r\n");
                        }
                        if (line.startsWith("USER")) {
                            out.write((userReply + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
                        } else if (line.startsWith("PASS")) {
                            out.write((passwordReply + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
                        } else {
                            out.write("200 ok\r\n".getBytes(StandardCharsets.ISO_8859_1));
                        }
                        out.flush();
                    }
                }
            } catch (final IOException e) {
                // Expected when the client disconnects.
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }
    }

    private static final class SentCommandRecorder implements ProtocolCommandListener {

        private final List<String> sentMessages = new ArrayList<>();

        @Override
        public void protocolCommandSent(final ProtocolCommandEvent event) {
            sentMessages.add(event.getMessage());
        }

        @Override
        public void protocolReplyReceived(final ProtocolCommandEvent event) {
            // not relevant here
        }
    }

    private static final String SECRET = "s3cr3t";

    private FakeFtpServer server;

    private FTPClient newConnectedClient() throws IOException {
        final FTPClient client = new FTPClient();
        client.connect("localhost", server.getPort());
        return client;
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) {
            server.close();
        }
    }

    private void startServer(final String userReply, final String passwordReply) throws IOException {
        server = new FakeFtpServer(userReply, passwordReply);
        server.start();
    }

    @Test
    void testLoginWithCharArrayClearsPasswordAfterSend() throws IOException {
        startServer("331 need password", "230 logged in");
        final char[] password = SECRET.toCharArray();
        final FTPClient client = newConnectedClient();
        try {
            assertTrue(client.login("user", password));
            // The password reached the server...
            assertTrue(server.getReceived().contains("PASS " + SECRET + "\r\n"));
            // ...and the array was cleared afterwards.
            assertArrayEquals(new char[SECRET.length()], password);
        } finally {
            client.disconnect();
        }
    }

    @Test
    void testLoginWithCharArrayDoesNotLeakToCommandListeners() throws IOException {
        startServer("331 need password", "230 logged in");
        final SentCommandRecorder recorder = new SentCommandRecorder();
        final FTPClient client = newConnectedClient();
        client.addProtocolCommandListener(recorder);
        try {
            assertTrue(client.login("user", SECRET.toCharArray()));
            assertTrue(server.getReceived().contains("PASS " + SECRET + "\r\n"));
            for (final String message : recorder.sentMessages) {
                assertFalse(message.contains(SECRET), () -> "Password leaked to listener: " + message);
            }
            // The listener still gets a useful, masked command event.
            assertTrue(recorder.sentMessages.stream().anyMatch(m -> m.contains("PASS ******")));
        } finally {
            client.disconnect();
        }
    }

    @Test
    void testLoginWithCharArrayRejectedStillClearsPassword() throws IOException {
        startServer("331 need password", "530 login incorrect");
        final char[] password = SECRET.toCharArray();
        final FTPClient client = newConnectedClient();
        try {
            assertFalse(client.login("user", password));
            assertArrayEquals(new char[SECRET.length()], password);
        } finally {
            client.disconnect();
        }
    }

    @Test
    void testLoginWithCharArrayKeepsPasswordWhenNotRequested() throws IOException {
        startServer("230 logged in", "230 logged in");
        final char[] password = SECRET.toCharArray();
        final FTPClient client = newConnectedClient();
        try {
            // The server accepts the user without asking for a password, so the array is untouched.
            assertTrue(client.login("user", password));
            assertArrayEquals(SECRET.toCharArray(), password);
            assertFalse(server.getReceived().contains("PASS"));
        } finally {
            client.disconnect();
        }
    }

    @Test
    void testPassWithCharArray() throws IOException {
        startServer("331 need password", "230 logged in");
        final FTPClient client = newConnectedClient();
        try {
            assertEquals(FTPReply.NEED_PASSWORD, client.user("user"));
            assertEquals(FTPReply.USER_LOGGED_IN, client.pass(SECRET.toCharArray()));
            assertTrue(server.getReceived().contains("PASS " + SECRET + "\r\n"));
        } finally {
            client.disconnect();
        }
    }
}
