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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FTPClientTransferModeTest {

    private static final class FtpServerAndPort {

        private final int port;
        private final FtpServer ftpServer;

        FtpServerAndPort(final FtpServer ftpServer, final int port) {
            this.port = port;
            this.ftpServer = ftpServer;
        }
    }

    @FunctionalInterface
    interface Runner {
        void run(int port, String user, String password) throws Exception;
    }

    private static final String DEFAULT_HOME = "ftp_root/";

    private static UserManager initUserManager(final String username, final String password) throws FtpException {

        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = new BaseUser();
        user.setName(username);
        user.setPassword(password);

        final List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);

        new File(DEFAULT_HOME).mkdirs();
        user.setHomeDirectory(DEFAULT_HOME);
        userManager.save(user);
        return userManager;
    }

    private static void runWithFTPserver(final Runner runner) throws Exception {
        final String userName = "test";
        final String password = "test";
        final FtpServerAndPort ftpServerAndPort = setupPlainFTPserver(userName, password);
        try {
            runner.run(ftpServerAndPort.port, userName, password);
        } finally {
            ftpServerAndPort.ftpServer.stop();
        }
    }

    private static FtpServerAndPort setupPlainFTPserver(final String username, final String password) throws FtpException {
        final FtpServerFactory serverFactory = new FtpServerFactory();

        // Init user
        serverFactory.setUserManager(initUserManager(username, password));

        final ListenerFactory factory = new ListenerFactory();
        // Automatically assign port.
        factory.setPort(0);

        // replace the default listener
        final Listener listener = factory.createListener();
        serverFactory.addListener("default", listener);

        // start the server
        final FtpServer server = serverFactory.createServer();
        server.start();

        return new FtpServerAndPort(server, listener.getPort());
    }

    @BeforeEach
    protected void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(DEFAULT_HOME));
    }

    @AfterEach
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File(DEFAULT_HOME));
    }

    @ParameterizedTest
    @ValueSource(ints = {FTP.DEFLATE_TRANSFER_MODE})
    public void testRetrievingFiles(final int transferMode) throws Exception {
        new File(DEFAULT_HOME).mkdirs();
        final String filename = "test_download.txt";
        final String fileContent = "Created at " + Instant.now();
        Files.write(Paths.get(DEFAULT_HOME).resolve(filename), fileContent.getBytes(StandardCharsets.UTF_8));

        runWithFTPserver((port, user, password) -> {
            final FTPClient client = new FTPClient();
            try {
                client.connect("localhost", port);
                client.login(user, password);
                assertTrue(client.setFileTransferMode(transferMode));

                final FTPFile[] files = client.listFiles();
                assertEquals(1, files.length);

                final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                assertTrue(client.retrieveFile(files[0].getName(), bos));
                assertEquals(fileContent, new String(bos.toByteArray(), StandardCharsets.UTF_8));
            } finally {
                client.logout();
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {FTP.DEFLATE_TRANSFER_MODE})
    public void testStoringFiles(final int transferMode) throws Exception {
        runWithFTPserver((port, user, password) -> {
            final FTPClient client = new FTPClient();
            try {
                client.connect("localhost", port);
                client.login(user, password);
                assertTrue(client.setFileTransferMode(transferMode));

                final FTPFile[] filesBeforeUpload = client.listFiles();
                assertEquals(0, filesBeforeUpload.length);

                final String fileName = "test_upload.txt";
                final String fileContent = "Created at " + Instant.now();
                assertTrue(client.storeFile(fileName, new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8))));

                final FTPFile[] filesAfterUpload = client.listFiles();
                assertEquals(1, filesAfterUpload.length);

                final Path p = Paths.get(DEFAULT_HOME, fileName);
                assertEquals(fileContent, new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
            } finally {
                client.logout();
            }
        });
    }

}
