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

package org.apache.commons.net.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Server socket factory for FTPS connections.
 *
 * @since 2.2
 */
public class FTPSServerSocketFactory extends ServerSocketFactory {

    /** Factory for secure socket factories */
    private final SSLContext sslContext;

    /**
     * Constructs a new instance for the given SSL context.
     *
     * @param sslContext The SSL context.
     */
    public FTPSServerSocketFactory(final SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @SuppressWarnings("resource") // Factory method.
    @Override
    public ServerSocket createServerSocket() throws IOException {
        return init(getServerSocketFactory().createServerSocket());
    }

    @SuppressWarnings("resource") // Factory method.
    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        return init(getServerSocketFactory().createServerSocket(port));
    }

    @SuppressWarnings("resource") // Factory method.
    @Override
    public ServerSocket createServerSocket(final int port, final int backlog) throws IOException {
        return init(getServerSocketFactory().createServerSocket(port, backlog));
    }

    @SuppressWarnings("resource") // Factory method.
    @Override
    public ServerSocket createServerSocket(final int port, final int backlog, final InetAddress ifAddress) throws IOException {
        return init(getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
    }

    private SSLServerSocketFactory getServerSocketFactory() {
        return this.sslContext.getServerSocketFactory();
    }

    /**
     * Sets the socket so newly accepted connections will use SSL client mode.
     *
     * @param socket the SSLServerSocket to initialize
     * @return the socket
     * @throws ClassCastException if socket is not an instance of SSLServerSocket
     */
    public ServerSocket init(final ServerSocket socket) {
        ((SSLServerSocket) socket).setUseClientMode(true);
        return socket;
    }
}
