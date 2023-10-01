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
package org.apache.commons.net.tftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.tftp.TFTPServer.ServerMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Basic tests to ensure that the TFTP Server is honoring its read/write mode, and preventing files from being read or written from outside of the assigned
 * roots.
 */
public class TFTPServerPathTest {

    private static final int SERVER_PORT = 6901;
    private static final String FILE_PREFIX = "TFTPServerPathTest_";

    private static Path createFileInDir(final Path directory, final String fileName) throws IOException {
        final Path filePath = directory.resolve(fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        return Files.createFile(filePath);
    }

    private static void deleteFile(final Path path, final boolean retry) throws IOException {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                if (retry) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        fail(e);
                    }
                    Files.deleteIfExists(path);
                }
                throw e;
            }
        }
    }

    private static String getRandomFileName(final String suffix) {
        return FILE_PREFIX + UUID.randomUUID() + suffix;
    }

    private TFTPServer tftpServer;
    private Path serverDirectory;

    private TFTPClient tftpClient;
    private Path fileToRead;
    private Path fileToWrite;

    @AfterEach
    public void afterEach() throws IOException {
        if (tftpClient != null) {
            tftpClient.close();
        }
        if (tftpServer != null) {
            tftpServer.close();
        }
        deleteFile(fileToRead, true);
        deleteFile(fileToWrite, true);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        serverDirectory = FileUtils.getTempDirectory().toPath();
        fileToRead = createFileInDir(serverDirectory, getRandomFileName(".source.txt"));
        fileToWrite = createFileInDir(serverDirectory, getRandomFileName(".out"));
    }

    private TFTPServer startTftpServer(final ServerMode serverMode) throws IOException {
        return new TFTPServer(serverDirectory.toFile(), serverDirectory.toFile(), SERVER_PORT, serverMode, null, null);
    }

    @Test
    public void testReadOnly() throws IOException {
        // Start a read-only server
        tftpServer = startTftpServer(ServerMode.GET_ONLY);
        final String serverAddress = "localhost";
        final int serverPort = tftpServer.getPort();

        // write some data to verify read
        Files.write(fileToRead, "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));
        final long fileToReadContentLength = Files.size(fileToRead);

        tftpClient = new TFTPClient();
        tftpClient.open();
        tftpClient.setSoTimeout(Duration.ofMillis(2000));

        // we can read file
        try (final OutputStream os = Files.newOutputStream(fileToWrite)) {
            final String writeFileName = fileToRead.getFileName().toString();
            final int bytesRead = tftpClient.receiveFile(writeFileName, TFTP.BINARY_MODE, os, serverAddress, serverPort);
            assertEquals(fileToReadContentLength, bytesRead);
        }

        // but we cannot write to it
        try (final InputStream is = Files.newInputStream(fileToRead)) {
            final String readFileName = fileToRead.getFileName().toString();
            final IOException exception = assertThrows(IOException.class, () -> tftpClient.sendFile(readFileName, TFTP.BINARY_MODE, is, serverAddress, serverPort));
            assertEquals("Error code 4 received: Write not allowed by server.", exception.getMessage());
        }
    }

    @Test
    public void testWriteOnly() throws IOException {
        // Start a write-only server
        tftpServer = startTftpServer(ServerMode.PUT_ONLY);
        final String serverAddress = "localhost";
        final int serverPort = tftpServer.getPort();

        tftpClient = new TFTPClient();
        tftpClient.open();
        tftpClient.setSoTimeout(Duration.ofMillis(2000));

        // we cannot read file
        try (final OutputStream os = Files.newOutputStream(fileToWrite)) {
            final String readFileName = fileToRead.getFileName().toString();
            final IOException exception = assertThrows(IOException.class, () -> tftpClient.receiveFile(readFileName, TFTP.BINARY_MODE, os, serverAddress, serverPort));
            assertEquals("Error code 4 received: Read not allowed by server.", exception.getMessage());
        }

        // but we can write to it
        try (final InputStream is = Files.newInputStream(fileToRead)) {
            deleteFile(fileToWrite, false);
            final String writeFileName = fileToWrite.getFileName().toString();
            tftpClient.sendFile(writeFileName, TFTP.BINARY_MODE, is, serverAddress, serverPort);
        }
    }

    @Test
    public void testWriteOutsideHome() throws IOException {
        // Start a read/write server
        tftpServer = startTftpServer(ServerMode.GET_AND_PUT);
        final String serverAddress = "localhost";
        final int serverPort = tftpServer.getPort();

        tftpClient = new TFTPClient();
        tftpClient.open();

        try (final InputStream is = Files.newInputStream(fileToRead)) {
            final IOException exception = assertThrows(IOException.class, () -> tftpClient.sendFile("../foo", TFTP.BINARY_MODE, is, serverAddress, serverPort));
            assertEquals("Error code 0 received: Cannot access files outside of TFTP server root.", exception.getMessage());
            assertFalse(Files.exists(serverDirectory.resolve("foo")), "file created when it should not have been");
        }
    }

    @Test
    public void testWriteVerifyContents() throws IOException {
        // Start a write-only server
        tftpServer = startTftpServer(ServerMode.GET_AND_PUT);
        final String serverAddress = "localhost";
        final int serverPort = tftpServer.getPort();

        tftpClient = new TFTPClient();
        tftpClient.open();
        tftpClient.setSoTimeout(Duration.ofMillis(2000));

        // write some data to file
        final byte[] content = "TFTP write test!".getBytes(StandardCharsets.UTF_8);
        Files.write(fileToRead, content);

        // send file
        try (final InputStream is = Files.newInputStream(fileToRead)) {
            deleteFile(fileToWrite, false);
            final String writeFileName = fileToWrite.getFileName().toString();
            tftpClient.sendFile(writeFileName, TFTP.BINARY_MODE, is, serverAddress, serverPort);
        }

        // then verify it contents
        try (final OutputStream os = Files.newOutputStream(fileToWrite)) {
            final String readFileName = fileToRead.getFileName().toString();
            final int readBytes = tftpClient.receiveFile(readFileName, TFTP.BINARY_MODE, os, serverAddress, serverPort);
            assertEquals(content.length, readBytes);
        }
    }

}
