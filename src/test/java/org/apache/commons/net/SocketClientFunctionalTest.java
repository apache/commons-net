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
 * A simple functional test class for SocketClients.
 *
 * Requires a Java-compatible SOCK proxy server on 127.0.0.1:9050 and access to ftp.gnu.org.
 */
public class SocketClientFunctionalTest extends TestCase
{
    // any subclass will do, but it should be able to connect to the destination host
    SocketClient sc = new FTPClient();
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 9050;
    private static final String DEST_HOST = "ftp.gnu.org";
    private static final int DEST_PORT = 21;

    /**
     * The constructor for this test case.
     * @param name passed to TestCase
     */
    public SocketClientFunctionalTest(String name)
    {
        super(name);
    }

    /**
     * A simple test to verify that the Proxy settings work.
     * @throws Exception in case of connection errors
     */
    public void testProxySettings() throws Exception
    {
        // NOTE: HTTP Proxies seem to be invalid for raw sockets
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
        sc.setProxy(proxy);
        sc.connect(DEST_HOST, DEST_PORT);
        assertTrue(sc.isConnected());
        sc.disconnect();
    }
}

