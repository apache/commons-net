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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;

/**
 * A simple test class for SocketClient settings.
 *
 * @since 3.2
 */
public class SocketClientTest {
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 1080;

    private static final String LOCALHOST_ADDRESS = "127.0.0.1";
    private static final String UNRESOLVED_HOST = "unresolved";
    private static final int REMOTE_PORT = 21;

    @Test
    public void testConnectResolved() {
        final SocketClient socketClient = new FTPClient();

        assertThrows(IOException.class, () -> socketClient.connect(LOCALHOST_ADDRESS, REMOTE_PORT));
        final InetSocketAddress remoteAddress = socketClient.getRemoteInetSocketAddress();
        assertFalse(remoteAddress.isUnresolved());
        assertEquals(LOCALHOST_ADDRESS, remoteAddress.getHostString());
        assertEquals(REMOTE_PORT, remoteAddress.getPort());
    }

    @Test
    public void testConnectUnresolved() {
        final SocketClient socketClient = new FTPClient();

        assertThrows(UnknownHostException.class, () -> socketClient.connect(UNRESOLVED_HOST, REMOTE_PORT, null, -1));
        final InetSocketAddress remoteAddress = socketClient.getRemoteInetSocketAddress();
        assertTrue(remoteAddress.isUnresolved());
        assertEquals(UNRESOLVED_HOST, remoteAddress.getHostString());
        assertEquals(REMOTE_PORT, remoteAddress.getPort());
    }

    /**
     * A simple test to verify that the Proxy is being set.
     */
    @Test
    public void testProxySettings() {
        final SocketClient socketClient = new FTPClient();
        assertNull(socketClient.getProxy());
        final Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        socketClient.setProxy(proxy);
        assertEquals(proxy, socketClient.getProxy());
        assertFalse(socketClient.isConnected());
    }
}
