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

import junit.framework.TestCase;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.commons.net.ftp.FTPClient;

/**
 * A simple test class for SocketClient settings.
 *
 * @since 3.2
 */
public class SocketClientTest extends TestCase
{
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 1080;

    /**
     * A simple test to verify that the Proxy is being set.
     */
    public void testProxySettings()
    {
        SocketClient socketClient = new FTPClient();
        assertNull(socketClient.getProxy());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        socketClient.setProxy(proxy);
        assertEquals(proxy, socketClient.getProxy());
        assertFalse(socketClient.isConnected());
    }
}
