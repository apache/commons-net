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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.net.PrintCommandListener;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.Assert;

/**
 * Tests {@link FTPSClient}.
 * <p>
 * To get our test certificate to work on Java 11, this test must be run with:
 * </p>
 *
 * <pre>
 * -Djdk.tls.client.protocols="TLSv1.1"
 * </pre>
 * <p>
 * This test does the above programmatically.
 * </p>
 */
public abstract class AbstractFtpsTest {

    private static int SocketPort;
    private static FtpServer EmbeddedFtpServer;
    protected static final boolean IMPLICIT = false;
    protected static final long TEST_TIMEOUT = 10000; // individual test timeout
    private static final boolean TRACE_CALLS = Boolean.parseBoolean(System.getenv("TRACE_CALLS"));
    private static final boolean ADD_LISTENER = Boolean.parseBoolean(System.getenv("ADD_LISTENER"));
    private static final long startTime = System.nanoTime();

    /**
     * Returns the test directory as a String.
     * @param defaultHome A default value.
     *
     * @return the test directory as a String
     */
    protected static String getTestHomeDirectory(final String defaultHome) {
        return System.getProperty("test.basedir", defaultHome);
    }

    /**
     * Creates and starts an embedded Apache MINA FTP Server.
     *
     * @param implicit FTPS connection mode.
     * @param userPropertiesResource resource path to user properties file.
     * @param serverJksResourceResource resource path to server JKS file.
     * @param defaultHome default home folder
     * @throws FtpException Thrown when the FTP classes cannot fulfill a request.
     */
    protected synchronized static void setupServer(final boolean implicit, final String userPropertiesResource, final String serverJksResourceResource, final String defaultHome)
            throws FtpException {
        if (EmbeddedFtpServer != null) {
            return;
        }
        // Let the OS find use an ephemeral port by using 0.
        SocketPort = 0;
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(userPropertiesResource);
        Assert.assertNotNull(userPropertiesResource, userPropsResource);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        // Pickup the home dir value at runtime even though we have it set in the userprop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(getTestHomeDirectory(defaultHome));
        serverFactory.setUserManager(userManager);
        final ListenerFactory factory = new ListenerFactory();
        factory.setPort(SocketPort);

        // define SSL configuration
        final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(serverJksResourceResource);
        Assert.assertNotNull(serverJksResourceResource, serverJksResource);
        // System.out.println("Loading " + serverJksResource);
        final SslConfigurationFactory sllConfigFactory = new SslConfigurationFactory();
        final File keyStoreFile = FileUtils.toFile(serverJksResource);
        Assert.assertTrue(keyStoreFile.toString(), keyStoreFile.exists());
        sllConfigFactory.setKeystoreFile(keyStoreFile);
        sllConfigFactory.setKeystorePassword("password");

        // set the SSL configuration for the listener
        final SslConfiguration sslConfiguration = sllConfigFactory.createSslConfiguration();
        final NoProtocolSslConfigurationProxy noProtocolSslConfigurationProxy = new NoProtocolSslConfigurationProxy(sslConfiguration);
        factory.setSslConfiguration(noProtocolSslConfigurationProxy);
        factory.setImplicitSsl(implicit);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        EmbeddedFtpServer = serverFactory.createServer();
        EmbeddedFtpServer.start();
        SocketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) EmbeddedFtpServer).getListener("default").getPort();
        // System.out.printf("jdk.tls.disabledAlgorithms = %s%n", System.getProperty("jdk.tls.disabledAlgorithms"));
        trace("Server started");
    }

    protected static void trace(final String msg) {
        if (TRACE_CALLS) {
            System.err.println(msg + " " + (System.nanoTime() - startTime));
        }
    }

    private final boolean endpointCheckingEnabled;

    public AbstractFtpsTest(final boolean endpointCheckingEnabled, final String userPropertiesResource, final String serverJksResource) {
        this.endpointCheckingEnabled = endpointCheckingEnabled;
    }

    protected void assertClientCode(final FTPSClient client) {
        final int replyCode = client.getReplyCode();
        assertTrue(FTPReply.isPositiveCompletion(replyCode));
    }

    protected FTPSClient loginClient() throws SocketException, IOException {
        trace(">>loginClient");
        final FTPSClient client = new FTPSClient(IMPLICIT);
        if (ADD_LISTENER) {
            client.addProtocolCommandListener(new PrintCommandListener(System.err));
        }
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
        //
        client.setEndpointCheckingEnabled(endpointCheckingEnabled);
        client.connect("localhost", SocketPort);
        //
        assertClientCode(client);
        assertEquals(SocketPort, client.getRemotePort());
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
        trace("<<loginClient");
        return client;
    }

    protected void retrieveFile(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // Do it twice.
            // Just testing that we are not getting an SSL error (the file MUST be present).
            assertTrue(pathname, client.retrieveFile(pathname, NullOutputStream.NULL_OUTPUT_STREAM));
            assertTrue(pathname, client.retrieveFile(pathname, NullOutputStream.NULL_OUTPUT_STREAM));
        } finally {
            client.disconnect();
        }
    }
}
