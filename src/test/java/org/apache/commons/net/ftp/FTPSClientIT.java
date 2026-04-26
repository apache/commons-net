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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.net.PrintCommandListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

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
@ParameterizedClass
@ValueSource(booleans = { false, true })
public class FTPSClientIT {

    private static final String FILEZILLA_PROPS_RES = "org/apache/commons/net/filezillaserver/filezillaserver.properties";
    private static final String LOGGING_PROPS_RES = "logging.properties";
    private static int FZSocketPort = -1;
    private static String FZServerHost = "";

    private static boolean UseExtFZServer;
    private static String ExtFZServerHost = "";
    private static int ExtFZServerPort = -1;

    private static final boolean ADD_LISTENER = Boolean.parseBoolean(System.getenv("ADD_LISTENER"));
    private static final boolean TRACE_CALLS = Boolean.parseBoolean(System.getenv("TRACE_CALLS"));
    private static final long startTime = System.nanoTime();
    protected static final boolean IMPLICIT = false;
    private static final Logger LOGGER = Logger.getLogger(FTPSClientIT.class.getPackage().getName());
    protected static final long TEST_TIMEOUT = 10000; // individual test timeout
    // Configure java.util.logging for lib bouncy castle
    // Redirect log to logback via org.slf4j.bridge.SLF4JBridgeHandler
    // So, we can check log to see if session really resumed
    static {
//        try {
//            final LogManager logManager = LogManager.getLogManager();
//            final URL logPropsFile = ClassLoader.getSystemClassLoader().getResource(LOGGING_PROPS_RES);
//            logManager.readConfiguration(logPropsFile.openStream());
//        } catch (final IOException exception) {
//            System.out.println("Cannot read configuration file " + LOGGING_PROPS_RES);
//            exception.printStackTrace();
//        }
    }

    protected static void initFZServer(final String fzPropertiesResource) throws IOException {
        // Make a try to activate SNI (using a real hostname for control connection)
        // with docker but don't work.
//        Map<String, String> hostAliases = new LinkedHashMap<>();
//        hostAliases.put("filezilla-server", "127.0.0.1");
//
//        // Installing the host resolvers
//        HostResolutionRequestInterceptor.INSTANCE.install(new MappedHostResolver(hostAliases),
//                // This is the system default resolving wrapper
//                DefaultHostResolver.INSTANCE);

        final URL fzPropsResource = ClassLoader.getSystemClassLoader().getResource(fzPropertiesResource);
        final Properties prop = new Properties();
        try (InputStream in = fzPropsResource.openStream()) {
            prop.load(in);
        }
        FZSocketPort = Integer.parseInt(prop.getProperty("filezillaserver.port"));
        FZServerHost = prop.getProperty("filezillaserver.host");

        UseExtFZServer = Boolean.valueOf(prop.getProperty("filezillaserver.external.enable"));
        if (UseExtFZServer) {
            ExtFZServerHost = prop.getProperty("filezillaserver.external.host");
            ExtFZServerPort = Integer.parseInt(prop.getProperty("filezillaserver.external.port"));
        }

    }

    @BeforeAll
    public static void setupServer() throws Exception {
        initFZServer(FILEZILLA_PROPS_RES);
    }

    public static Boolean[] testConstructurData() {
        return new Boolean[] { Boolean.FALSE, Boolean.TRUE };
    }

    protected static void trace(final String msg) {
        if (TRACE_CALLS) {
            System.err.println(msg + " " + (System.nanoTime() - startTime));
        }
    }

    private final boolean endpointCheckingEnabled;

    public FTPSClientIT(final boolean endpointCheckingEnabled) {
        this.endpointCheckingEnabled = endpointCheckingEnabled;
    }

    protected void assertClientCode(final FTPSClient client) {
        final int replyCode = client.getReplyCode();
        assertTrue(FTPReply.isPositiveCompletion(replyCode));
    }

    // Only passive mode tested
    protected FTPSClient loginClientTo(final String hostname, final int port, final boolean withSSLSessionReuse) throws Exception {
        trace(">>loginClientTo");
        LOGGER.fine("Server " + FZServerHost);
        LOGGER.fine("Port " + FZSocketPort);
        FTPSClient client = null;
        if (withSSLSessionReuse) {
            client = new FTPSClientSSLSessionReuse(IMPLICIT);
        } else {
            client = new FTPSClient(IMPLICIT);
        }
        if (ADD_LISTENER) {
            client.addProtocolCommandListener(new PrintCommandListener(System.err));
        }

        // HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        //
        client.setControlKeepAliveReplyTimeout(null);
        assertEquals(0, client.getControlKeepAliveReplyTimeoutDuration().getSeconds());
        client.setControlKeepAliveReplyTimeout(Duration.ofSeconds(60));
        assertEquals(60, client.getControlKeepAliveReplyTimeoutDuration().getSeconds());
        //
        client.setControlKeepAliveTimeout(null);
        assertEquals(0, client.getControlKeepAliveTimeoutDuration().getSeconds());
        client.setControlKeepAliveTimeout(Duration.ofSeconds(61));
        assertEquals(61, client.getControlKeepAliveTimeoutDuration().getSeconds());
        //
        client.setDataTimeout(null);
        assertEquals(0, client.getDataTimeout().getSeconds());
        client.setDataTimeout(Duration.ofSeconds(62));
        assertEquals(62, client.getDataTimeout().getSeconds());

        client.setUseClientMode(true);
        //
        client.setEndpointCheckingEnabled(endpointCheckingEnabled);
        client.connect(hostname, port);
        //
        assertClientCode(client);
        assertEquals(port, client.getRemotePort());
        //
        try {
            // HACK: Without this sleep, the user command sometimes does not reach the ftpserver
            // This only seems to affect GitHub builds, and only Java 11+
            Thread.sleep(200); // 100 seems to be not always enough
        } catch (final InterruptedException ignore) {
            // ignore
        }
        assertTrue(client.login("test", "test"));
        assertClientCode(client);
        //
        client.setFileType(FTP.BINARY_FILE_TYPE);
        assertClientCode(client);
        //
        client.execPBSZ(0);
        assertClientCode(client);
        //
        client.execPROT("P");
        assertClientCode(client);
        //
        // Only passive mode tested
        client.enterLocalPassiveMode();

        trace("<<loginClientTo");
        return client;
    }

    /**
     * Login to Docker FTPS FileZilla Server
     * @param withSSLSessionReuse enable Bouncy Castle library if true
     * @return
     * @throws Exception
     */
    protected FTPSClient loginClientToFZ(final boolean withSSLSessionReuse) throws Exception {
        trace(">>loginClientToFZ");
        assertNotEquals(FZSocketPort, -1, "initFZServer not called");
        final FTPSClient client = loginClientTo(FZServerHost, FZSocketPort, withSSLSessionReuse);
        trace("<<loginClientToFZ");
        return client;
    }

    protected void retrieveFile(final String pathname, final boolean withSSLResume) throws Exception {
        final FTPSClient client = loginClientToFZ(withSSLResume);
        try {
            // Do it twice.
            // Just testing that we are not getting an SSL error (the file MUST be present).
            assertTrue(client.retrieveFile(pathname, NullOutputStream.INSTANCE), pathname);
            assertTrue(client.retrieveFile(pathname, NullOutputStream.INSTANCE), pathname);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Test on external filezilla server. Test Should be OK with SNI, should be wrong without SNI. Work only when FTPSClient.prepareDataSocket called before
     * connect in openDataSecureConnection(String, String).
     * @param pathname
     * @param withSni
     * @throws Exception
     */
    protected void retrieveFileOnExtServer(final String pathname, final boolean withSni) throws Exception {
        final FTPSClient client = loginClientTo(ExtFZServerHost, ExtFZServerPort, true);
        try {
            // Do it twice.
            // Just testing that we are not getting an SSL error (the file MUST be present).
            if (withSni) {
                // set same peerHost in data socket
                ((FTPSClientSSLSessionReuse) client).setNeedHostForSni(true);
                assertTrue(client.retrieveFile(pathname, NullOutputStream.INSTANCE), pathname);
                assertTrue(client.retrieveFile(pathname, NullOutputStream.INSTANCE), pathname);
            } else {
                // Strange, testing on real exception don't work : org.apache.commons.net.io.CopyStreamException
                assertThrows(Exception.class, () -> client.retrieveFile(pathname, NullOutputStream.INSTANCE));
            }
        } finally {
            client.disconnect();
        }
    }

    /**
     * This test is corrected by this Merge Request. It requires an external server. The docker version and its bridge network cannot activate SNI verifications
     * @throws Exception
     */
    @Test
    @Timeout(TEST_TIMEOUT)
    public void testExtFileZillaServerForSNI() throws Exception {
        assumeTrue(UseExtFZServer);
        trace(">>testExtFileZillaServerForSNI");
        retrieveFileOnExtServer("/file.txt", true);
        trace("<<testExtFileZillaServerForSNI");
    }

    /**
     * This test is fixed by this Merge Request. Need external server. Docker version and its bridge network cannot active SNI verifications
     * @throws Exception
     */
    @Test
    @Timeout(TEST_TIMEOUT)
    public void testExtFileZillaServerNoSNI() throws Exception {
        assumeTrue(UseExtFZServer);
        trace(">>testExtFileZillaServerNoSNI");
        retrieveFileOnExtServer("/file.txt", false);
        trace("<<testExtFileZillaServerNoSNI");
    }

    /**
     * This test will fail, it's normal, need TLS Resume (need Bouncy Castle)
     * @throws Exception
     */
    @Test
    @Timeout(TEST_TIMEOUT)
    public void testFileZillaNoTlsResume() throws Exception {
        trace(">>testFileZillaNoTlsResume");
        final FTPSClient client = loginClientToFZ(false);
        try {
            // Do it twice.
            // Just testing that we are not getting an SSL error (the file MUST be present).
            final String string = "/file.txt";
            assertFalse(client.retrieveFile(string, NullOutputStream.INSTANCE), string);
            assertFalse(client.retrieveFile(string, NullOutputStream.INSTANCE), string);
        } finally {
            client.disconnect();
        }
        trace("<<testFileZillaNoTlsResume");
    }

    @Test
    @Timeout(TEST_TIMEOUT)
    public void testFileZillaTlsResume() throws Exception {
        trace(">>testFileZillaTlsResume");
        retrieveFile("/file.txt", true);
        trace("<<testFileZillaTlsResume");
    }

}
