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

package org.apache.commons.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class MockTcpServer implements Runnable, Closeable {

    private volatile boolean running;
    private final int port;
    private final InetAddress serverAddress;
    private final ServerSocket serverSocket;
    private final Thread serverThread;

    protected MockTcpServer() throws IOException {
        this(0);
    }

    protected MockTcpServer(final int port) throws IOException {
        this(port, InetAddress.getLocalHost());
    }

    protected MockTcpServer(final int port, final InetAddress serverAddress) throws IOException {
        this.serverAddress = serverAddress;
        this.serverSocket = new ServerSocket(port, 50, serverAddress);
        this.port = serverSocket.getLocalPort();
        this.serverThread = new Thread(this);
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    public int getPort() {
        return this.port;
    }

    protected abstract void processClientSocket(Socket clientSocket) throws Exception;

    @Override
    public final void run() {
        while (running) {
            try (final Socket clientSocket = serverSocket.accept()) {
                processClientSocket(clientSocket);
            } catch (final IOException e) {
                System.err.println("IOException on MockWebServer serverSocket.accept(): " + e);
            } catch (final Exception e) {
                System.err.println("MockWebServer serverSocket.accept() error: " + e);
            }
        }
    }

    public synchronized void start() throws IOException {
        System.out.println("Starting MockWebServer...");
        if (!running) {
            running = true;
            this.serverThread.start();
            System.out.println("Successfully started MockWebServer on address " + this.serverAddress.getHostAddress() + " and port " + this.port);
        }
    }

    public synchronized void stop() throws IOException {
        System.out.println("Closing MockWebServer...");
        if (!running) {
            return;
        }
        running = false;
        serverThread.interrupt();
        try {
            serverSocket.close();
            System.out.println("Successfully closed MockWebServer!");
        } catch (final IOException ioException) {
            System.err.println("Could not stop MockWebServer, cause: " + ioException.getMessage());
            throw ioException;
        }
    }

}
