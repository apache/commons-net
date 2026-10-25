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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

class FTPHTTPClientTest {

    private static String readRequestLine(final BufferedReader reader) throws IOException {
        final String requestLine = reader.readLine();
        for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
            // skip headers
        }
        return requestLine;
    }

    private static void write(final OutputStream out, final String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    /**
     * A PASV data connection must be tunneled to the FTP server, not to the proxy's own address on a port chosen by the FTP server.
     */
    @Test
    void testPassiveDataConnectionTunnelsToControlHost() throws Exception {
        final List<String> connectLines = new CopyOnWriteArrayList<>();
        try (ServerSocket proxy = new ServerSocket(0, 2, InetAddress.getLoopbackAddress())) {
            final Thread server = new Thread(() -> {
                try (Socket control = proxy.accept()) {
                    final BufferedReader in = new BufferedReader(new InputStreamReader(control.getInputStream(), StandardCharsets.US_ASCII));
                    final OutputStream out = control.getOutputStream();
                    connectLines.add(readRequestLine(in));
                    write(out, "HTTP/1.1 200 Connection established\r\n\r\n");
                    write(out, "220 ready\r\n");
                    for (String cmd = in.readLine(); cmd != null; cmd = in.readLine()) {
                        if (cmd.startsWith("PASV")) {
                            write(out, "227 Entering Passive Mode (10,1,2,3,4,1).\r\n");
                            try (Socket data = proxy.accept()) {
                                final BufferedReader dataIn = new BufferedReader(new InputStreamReader(data.getInputStream(), StandardCharsets.US_ASCII));
                                connectLines.add(readRequestLine(dataIn));
                                write(data.getOutputStream(), "HTTP/1.1 403 Forbidden\r\n\r\n");
                            }
                        } else {
                            write(out, "500 unknown\r\n");
                        }
                    }
                } catch (final IOException ignored) {
                    // test ends
                }
            });
            server.setDaemon(true);
            server.start();

            final FTPHTTPClient client = new FTPHTTPClient(proxy.getInetAddress().getHostAddress(), proxy.getLocalPort());
            client.setDefaultTimeout(10_000);
            client.connect("ftp.example.invalid", 21);
            try {
                client.enterLocalPassiveMode();
                assertThrows(IOException.class, client::listNames);
            } finally {
                client.disconnect();
            }
            server.join(10_000);
        }
        assertEquals(2, connectLines.size(), connectLines::toString);
        assertEquals("CONNECT ftp.example.invalid:21 HTTP/1.1", connectLines.get(0));
        assertEquals("CONNECT ftp.example.invalid:1025 HTTP/1.1", connectLines.get(1));
    }
}
