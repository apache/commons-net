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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.time.Instant;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Tests {@link FTPSClient}.
 * <p>
 * To get our test cert to work on Java 11, this test must be run with:
 * </p>
 *
 * <pre>
 * -Djdk.tls.client.protocols="TLSv1.1"
 * </pre>
 * <p>
 * This test does the above programmatically.
 * </p>
 */
@RunWith(Parameterized.class)
public class FTPSProxyClientTest extends AbstractFtpsTest {

    private static final String USER_PROPS_RES = "org/apache/commons/net/ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org/apache/commons/net/ftpsserver/ftpserver.jks";

    @BeforeClass
    public static void setupServer() throws Exception {
        setupServer(IMPLICIT, USER_PROPS_RES, SERVER_JKS_RES, "target/test-classes/org/apache/commons/net/test-data");
    }


    @Parameters(name = "endpointCheckingEnabled={0}")
    public static Boolean[] testConstructurData() {
        return new Boolean[] { Boolean.FALSE, Boolean.TRUE };
    }

    public FTPSProxyClientTest(final boolean endpointCheckingEnabled) {
        super(endpointCheckingEnabled, null, null);
    }

    public final String HOSTNAME = "localhost";
    public final int PORT = 21000;
    private final Integer PROXYPORT = 3128;
    private final Proxy PROXY =
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HOSTNAME, PROXYPORT));

    @Test(timeout = TEST_TIMEOUT)
    public void testListFiles() throws SocketException, IOException {
        //final FTPSClient client = loginClient();
        FTPSClient client = new FTPSClient();
        try {

            client.setProxy(PROXY);
            client.connect(HOSTNAME, PORT);

            client.login("username", "userpass");

            if (client.getReplyCode() == 530) {
                trace(">>unable to login");
            }

            client.enterLocalPassiveMode();

            FTPFile[] files = client.listFiles();

            System.out.println(files.length);

        } finally {
            client.disconnect();
        }
    }

}
