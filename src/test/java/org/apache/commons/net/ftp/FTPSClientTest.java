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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class FTPSClientTest {

    private static final String JDK_TLS_CLIENT_PROTOCOLS = "jdk.tls.client.protocols";

    private static int SocketPort;

    private static String ConnectionUri;

    private static FtpServer Server;

    private static final String USER_PROPS_RES = "org/apache/commons/net/ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org/apache/commons/net/ftpsserver/ftpserver.jks";

    private static final boolean implicit = false;

    private static String TlsProtocols;

    @AfterClass
    public static void afterClass() {
        if (TlsProtocols == null) {
            System.getProperties().remove(JDK_TLS_CLIENT_PROTOCOLS);
        } else {
            System.setProperty(JDK_TLS_CLIENT_PROTOCOLS, TlsProtocols);
        }
    }

    /**
     * Returns the test directory as a String.
     * 
     * @return the test directory as a String
     */
    private static String getTestHomeDirectory() {
        return System.getProperty("test.basedir", "target/test-classes/org/apache/commons/net/test-data");
    }

    @BeforeClass
    public static void setUp() throws Exception {
        setUpClass(implicit);
    }

    @BeforeClass
    public static void setUpClass() {
        TlsProtocols = System.getProperty(JDK_TLS_CLIENT_PROTOCOLS);
        System.setProperty(JDK_TLS_CLIENT_PROTOCOLS, "TLSv1");
    }

    /**
     * Creates and starts an embedded Apache MINA FTP Server.
     *
     * @param implicit FTPS connection mode
     * @throws FtpException
     * @throws IOException
     */
    static void setUpClass(final boolean implicit) throws FtpException, IOException {
        if (Server != null) {
            return;
        }
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
        final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES);
        Assert.assertNotNull(SERVER_JKS_RES, serverJksResource);
        final SslConfigurationFactory sllConfigFactory = new SslConfigurationFactory();
        final File keyStoreFile = FileUtils.toFile(serverJksResource);
        Assert.assertTrue(keyStoreFile.toString(), keyStoreFile.exists());
        sllConfigFactory.setKeystoreFile(keyStoreFile);
        sllConfigFactory.setKeystorePassword("password");
        sllConfigFactory.setSslProtocol("TLSv1.1");

        // set the SSL configuration for the listener
        factory.setSslConfiguration(sllConfigFactory.createSslConfiguration());
        factory.setImplicitSsl(implicit);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        Server = serverFactory.createServer();
        Server.start();
        SocketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) Server).getListener("default").getPort();
        ConnectionUri = "ftps://test:test@localhost:" + SocketPort;
        System.out.printf("jdk.tls.disabledAlgorithms = %s%n", System.getProperty("jdk.tls.disabledAlgorithms"));
    }

    private void assertClientCode(final FTPSClient client) {
        final int replyCode = client.getReplyCode();
        assertTrue(FTPReply.isPositiveCompletion(replyCode));
    }

    private FTPSClient loginClient() throws SocketException, IOException {
        final FTPSClient client = new FTPSClient(implicit);
        client.setEndpointCheckingEnabled(true);
        client.connect("localhost", SocketPort);
        assertClientCode(client);
        assertEquals(SocketPort, client.getRemotePort());
        //
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
            client.disconnect();
        }
    }

    private void testListFiles(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            assertNotNull(client.listFiles(pathname));
            assertNotNull(client.listFiles(pathname));
        } finally {
            client.disconnect();
        }
    }

    @Test
    public void testListFilesPathNameEmpty() throws SocketException, IOException {
        testListFiles("");
    }

    @Test
    public void testListFilesPathNameJunk() throws SocketException, IOException {
        testListFiles("   Junk   ");
    }

    @Test
    public void testListFilesPathNameNull() throws SocketException, IOException {
        testListFiles(null);
    }

    @Test
    public void testListFilesPathNameRoot() throws SocketException, IOException {
        testListFiles("/");
    }

    @Test
    public void testOpenClose() throws SocketException, IOException {
        loginClient().disconnect();
    }

    @Test
    public void testRetrieveFilePathNameRoot() throws SocketException, IOException {
        retrieveFile("/file.txt");
    }
}
