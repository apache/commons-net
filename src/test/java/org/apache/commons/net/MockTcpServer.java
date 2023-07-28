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

/**
 * The MockTcpServer class is a simple TCP implementation of a server for the different TCP related protocols.
 * <p>
 * To use this class simply create a new class that will extend {@link MockTcpServer} and implement
 * {@link MockTcpServer#processClientSocket(Socket)} method.
 * <p>
 * Example usage:
 * <code>
 * final class HelloWorldTCPServer extends MockTcpServer {
 *      // other fields and constructors are omitted for brevity
 *     {@literal @Override}
 *     protected void processClientSocket(final Socket clientSocket) throws Exception {
 *         try (final PrintWriter pw = new PrintWriter(clientSocket.getOutputStream())) {
 *             pw.write("Hello, World!");
 *             pw.flush();
 *         }
 *     }
 * }
 * </code>
 * <p>NOTE: this is for <B>debugging and testing purposes only</B> and not meant to be run as a reliable server.</p>
 * @see org.apache.commons.net.daytime.MockDaytimeTCPServer MockDaytimeTCPServer (for example Daytime Protocol implementation)
 */
public abstract class MockTcpServer implements Runnable, Closeable {

    /** Flag that indicates whether server is running */
    protected volatile boolean running;

    /** {@link InetAddress} on which server is bound to */
    protected final InetAddress serverAddress;

    /** {@link ServerSocket} on which server listens */
    protected final ServerSocket serverSocket;

    /** Port number on which {@link #serverSocket} is listening */
    protected final int port;

    /** {@link Thread} that server is running on */
    protected final Thread serverThread;

    /**
     * Creates new {@link MockTcpServer} that will bind to {@link InetAddress#getLocalHost()}
     * on random port.
     *
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    protected MockTcpServer() throws IOException {
        this(0);
    }

    /**
     * Creates new {@link MockTcpServer} that will bind to {@link InetAddress#getLocalHost()}
     * on specified port.
     *
     * @param port the port number the server will bind to, or 0 to use a port number that is automatically allocated
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    protected MockTcpServer(final int port) throws IOException {
        this(port, InetAddress.getLocalHost());
    }

    /**
     * Creates new {@link MockTcpServer} that will bind to specified {@link InetAddress} and on specified port.
     *
     * @param port the port number the server will bind to, or 0 to use a port number that is automatically allocated
     * @param serverAddress the InetAddress the server will bind to
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    protected MockTcpServer(final int port, final InetAddress serverAddress) throws IOException {
        this.serverAddress = serverAddress;
        this.serverSocket = new ServerSocket(port, 50, serverAddress);
        this.port = serverSocket.getLocalPort();
        this.serverThread = new Thread(this);
    }

    /**
     * Closes server socket and stop listening.
     * Calling this method will have the same effect as {@link MockTcpServer#stop()}
     *
     * @throws IOException If an I/O error occurs while closing the {@link #serverSocket}.
     * @see MockTcpServer#stop()
     */
    @Override
    public void close() throws IOException {
        stop();
    }

    /**
     * Gets the port number on which {@link #serverSocket} is listening
     *
     * @return the port number to which this socket is listening or -1
     *         if the socket is not bound yet
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Processes next client {@link Socket} from this server.
     *
     * @implNote there is no need to manually close {@code clientSocket}, it will be closed for you
     * @param clientSocket next accepted {@link Socket} you can use. Never {@code null}
     * @throws Exception in case of any error
     */
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

    /**
     * Starts the server and begins to listen on {@link #serverSocket}
     */
    public synchronized void start() {
        System.out.println("Starting MockWebServer...");
        if (!running) {
            running = true;
            this.serverThread.start();
            System.out.println("Successfully started MockWebServer on address " + this.serverAddress.getHostAddress() + " and port " + this.port);
        }
    }

    /**
     * Closes server socket and stop listening.
     *
     * @throws IOException If an I/O error occurs while closing the {@link #serverSocket}.
     * @see MockTcpServer#close()
     */
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

