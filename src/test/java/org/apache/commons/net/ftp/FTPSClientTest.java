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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
public class FTPSClientTest {

    private static int SocketPort;

    private static FtpServer EmbeddedFtpServer;

    private static final String USER_PROPS_RES = "org/apache/commons/net/ftpsserver/users.properties";

    // TEMPORARY hack to see if old JKS works
    private static final String SERVER_JKS_RES_JRE_8 = "org/apache/commons/net/ftpsserver/ftpserver-old.jks";

    private static final String SERVER_JKS_RES_JRE_16 = "org/apache/commons/net/ftpsserver/ftpserver-old.jks";

    private static final boolean IMPLICIT = false;

    /**
     * Returns the test directory as a String.
     *
     * @return the test directory as a String
     */
    private static String getTestHomeDirectory() {
        return System.getProperty("test.basedir", "target/test-classes/org/apache/commons/net/test-data");
    }

    private static final long TEST_TIMEOUT = 10000; // individual test timeout

    private static final long startTime = System.nanoTime();
    private static void trace(String msg) {
        // System.err.println(msg + " " + (System.nanoTime() - startTime));
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        trace(">>setUpClass");
        setUpClass(IMPLICIT);
        trace("<<setUpClass");
    }

    /**
     * Creates and starts an embedded Apache MINA FTP Server.
     *
     * @param IMPLICIT FTPS connection mode
     * @throws FtpException
     */
    private synchronized static void setUpClass(final boolean implicit) throws FtpException {
        if (EmbeddedFtpServer != null) {
            trace("Server already active");
            return;
        }
        trace("Server startup");
        // Use an ephemeral port.
        SocketPort = 0;
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assert.assertNotNull(USER_PROPS_RES, userPropsResource);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        // Pickup the home dir value at runtime even though we have it set in the user
        // prop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(getTestHomeDirectory());
        serverFactory.setUserManager(userManager);
        final ListenerFactory factory = new ListenerFactory();
        // set the port of the listener
        factory.setPort(SocketPort);

        // define SSL configuration
        final URL serverJksResource = SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_16)
            ? ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES_JRE_16)
            : ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES_JRE_8);
        System.out.println("Loading " + serverJksResource);
        Assert.assertNotNull(SERVER_JKS_RES_JRE_8, serverJksResource);
        final SslConfigurationFactory sllConfigFactory = new SslConfigurationFactory();
        final File keyStoreFile = FileUtils.toFile(serverJksResource);
        Assert.assertTrue(keyStoreFile.toString(), keyStoreFile.exists());
        sllConfigFactory.setKeystoreFile(keyStoreFile);
        sllConfigFactory.setKeystorePassword("password");

        // set the SSL configuration for the listener
        factory.setSslConfiguration(sllConfigFactory.createSslConfiguration());
        factory.setImplicitSsl(implicit);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        trace("Server starting");
        EmbeddedFtpServer = serverFactory.createServer();
        EmbeddedFtpServer.start();
        SocketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) EmbeddedFtpServer).getListener("default").getPort();
        // System.out.printf("jdk.tls.disabledAlgorithms = %s%n", System.getProperty("jdk.tls.disabledAlgorithms"));
        trace("Server started");
    }

    @Parameters(name = "endpointCheckingEnabled={0}")
    public static Boolean[] testConstructurData() {
        return new Boolean[] {Boolean.FALSE, Boolean.TRUE};
    }

    private final boolean endpointCheckingEnabled;

    public FTPSClientTest(final boolean endpointCheckingEnabled) {
        this.endpointCheckingEnabled = endpointCheckingEnabled;
    }

    private void assertClientCode(final FTPSClient client) {
        final int replyCode = client.getReplyCode();
        assertTrue(FTPReply.isPositiveCompletion(replyCode));
    }

    private FTPSClient loginClient() throws SocketException, IOException {
        trace(">>loginClient");
        try {
            Thread.sleep(5000); // See if a short sleep between logins helps
        } catch (InterruptedException e) {};
        final FTPSClient client = new FTPSClient(IMPLICIT);
        client.addProtocolCommandListener(new PrintCommandListener(System.err));
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
        trace(">>loginClient-connect");
        client.connect("localhost", SocketPort);
        //
        assertClientCode(client);
        assertEquals(SocketPort, client.getRemotePort());
        //
        trace(">>loginClient-login");
        assertTrue(client.login("test", "test"));
        assertClientCode(client);
        //
        trace(">>loginClient-binary");
        client.setFileType(FTP.BINARY_FILE_TYPE);
        assertClientCode(client);
        //
        trace(">>loginClientPBSZ");
        client.execPBSZ(0);
        assertClientCode(client);
        //
        trace(">>loginClientPROT");
        client.execPROT("P");
        assertClientCode(client);
        trace("<<loginClient");
        return client;
    }

    private void retrieveFile(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // Do it twice.
            // Just testing that we are not getting an SSL error (the file MUST be present).
            assertTrue(pathname, client.retrieveFile(pathname, NullOutputStream.NULL_OUTPUT_STREAM));
            assertTrue(pathname, client.retrieveFile(pathname, NullOutputStream.NULL_OUTPUT_STREAM));
        } finally {
            trace(">>disconnect");
            client.disconnect();
            trace("<<disconnect");
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testHasFeature() throws SocketException, IOException {
        trace(">>testHasFeature");
        loginClient().disconnect();
        trace("<<testHasFeature");
    }

    private void testListFiles(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            assertNotNull(client.listFiles(pathname));
            assertNotNull(client.listFiles(pathname));
        } finally {
            trace(">>disconnect");
            client.disconnect();
            trace("<<disconnect");
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testListFilesPathNameEmpty() throws SocketException, IOException {
        trace(">>testListFilesPathNameEmpty");
        testListFiles("");
        trace("<<testListFilesPathNameEmpty");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testListFilesPathNameJunk() throws SocketException, IOException {
        trace(">>testListFilesPathNameJunk");
        testListFiles("   Junk   ");
        trace("<<testListFilesPathNameJunk");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testListFilesPathNameNull() throws SocketException, IOException {
        trace(">>testListFilesPathNameNull");
        testListFiles(null);
        trace("<<testListFilesPathNameNull");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testListFilesPathNameRoot() throws SocketException, IOException {
        trace(">>testListFilesPathNameRoot");
        testListFiles("/");
        trace("<<testListFilesPathNameRoot");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testMdtmCalendar() throws SocketException, IOException {
        trace(">>testMdtmCalendar");
        testMdtmCalendar("/file.txt");
        trace("<<testMdtmCalendar");
    }

    private void testMdtmCalendar(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            final Calendar mdtmCalendar1 = client.mdtmCalendar(pathname);
            final Calendar mdtmCalendar2 = client.mdtmCalendar(pathname);
            assertNotNull(mdtmCalendar1);
            assertNotNull(mdtmCalendar2);
            assertEquals(mdtmCalendar1, mdtmCalendar2);
        } finally {
            trace(">>disconnect");
            client.disconnect();
            trace("<<disconnect");
        }
    }

    public void testMdtmFile() throws SocketException, IOException {
        testMdtmFile("/file.txt");
    }

    private void testMdtmFile(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            final FTPFile mdtmFile1 = client.mdtmFile(pathname);
            final FTPFile mdtmFile2 = client.mdtmFile(pathname);
            assertNotNull(mdtmFile1);
            assertNotNull(mdtmFile2);
            assertEquals(mdtmFile1, mdtmFile2);
        } finally {
            trace(">>disconnect");
            client.disconnect();
            trace("<<disconnect");
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testMdtmInstant() throws SocketException, IOException {
        trace(">>testMdtmInstant");
        testMdtmInstant("/file.txt");
        trace("<<testMdtmInstant");
    }

    private void testMdtmInstant(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            final Instant mdtmInstant1 = client.mdtmInstant(pathname);
            final Instant mdtmInstant2 = client.mdtmInstant(pathname);
            assertNotNull(mdtmInstant1);
            assertNotNull(mdtmInstant2);
            assertEquals(mdtmInstant1, mdtmInstant2);
        } finally {
            trace(">>disconnect");
            client.disconnect();
            trace("<<disconnect");
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testOpenClose() throws SocketException, IOException {
        trace(">>testOpenClose");
        final FTPSClient ftpsClient = loginClient();
        try {
            assertTrue(ftpsClient.hasFeature("MODE"));
            assertTrue(ftpsClient.hasFeature(FTPCmd.MODE));
        } finally {
            trace(">>disconnect");
            ftpsClient.disconnect();
            trace("<<disconnect");
        }
        trace("<<testOpenClose");
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testRetrieveFilePathNameRoot() throws SocketException, IOException {
        trace(">>testRetrieveFilePathNameRoot");
        retrieveFile("/file.txt");
        trace("<<testRetrieveFilePathNameRoot");
    }
}
