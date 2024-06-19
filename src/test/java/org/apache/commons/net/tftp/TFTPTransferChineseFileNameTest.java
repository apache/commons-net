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

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.tftp.TFTPServer.ServerMode;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the TFTP Server and TFTP Client by creating some FILES in the system temp folder and then uploading and downloading them.
 */
public class TFTPTransferChineseFileNameTest extends TestCase {
    private static final int SERVER_PORT = 6902;
    private static TFTPServer tftpS;
    private static final File SERVER_DIR = FileUtils.getTempDirectory();
    private static final String FILE_PREFIX = "tftp-";
    private static final File[] FILES = new File[8];

    static int testsLeftToRun = 9; // TODO Nasty hack.

    // only want to do this once...
    static {
        try {
            FILES[0] = createFile(new File(SERVER_DIR, FILE_PREFIX + "空.txt"), 0);
            FILES[1] = createFile(new File(SERVER_DIR, FILE_PREFIX + "小.txt"), 1);
            FILES[2] = createFile(new File(SERVER_DIR, FILE_PREFIX + "五百一十一.txt"), 511);
            FILES[3] = createFile(new File(SERVER_DIR, FILE_PREFIX + "五百一十二.txt"), 512);
            FILES[4] = createFile(new File(SERVER_DIR, FILE_PREFIX + "五百一十三.txt"), 513);
            FILES[5] = createFile(new File(SERVER_DIR, FILE_PREFIX + "中.txt"), 1000 * 1024);
            FILES[6] = createFile(new File(SERVER_DIR, FILE_PREFIX + "大.txt"), 5000 * 1024);
            FILES[7] = createFile(new File(SERVER_DIR, FILE_PREFIX + "巨大.txt"), 37000 * 1024);

            // Start the server
            tftpS = new TFTPServer(SERVER_DIR, SERVER_DIR, SERVER_PORT, ServerMode.GET_AND_PUT, null, null);
            tftpS.setSocketTimeout(2000);
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * Create a file, size specified in bytes
     */
    private static File createFile(final File file, final int size) throws IOException {
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
            final byte[] temp = "0".getBytes();
            for (int i = 0; i < size; i++) {
                os.write(temp);
            }
        }
        return file;
    }

    private boolean contentEquals(final File a, final File b) throws IOException {
        return FileUtils.contentEquals(a, b);
    }

    @Override
    protected void tearDown() throws Exception {
        testsLeftToRun--;
        if (testsLeftToRun <= 0) {
            if (tftpS != null) {
                tftpS.close();
            }
            for (final File file : FILES) {
                file.delete();
            }
        }
        super.tearDown();
    }

    public void testASCIIDownloads() {
        // test with the smaller FILES
        for (int i = 0; i < 6; i++) {
            try {
                testDownload(TFTP.ASCII_MODE, FILES[i]);
            } catch (final IOException e) {
                fail("Entry " + i + " Error " + e.toString());
            }

        }
    }

    public void testASCIIUploads() throws Exception {
        // test with the smaller FILES
        for (int i = 0; i < 6; i++) {
            testUpload(TFTP.ASCII_MODE, FILES[i]);
        }
    }

    public void testDiscardPackets() throws IOException {
        try (TFTP tftp = new TFTP()) {
            assertThrows(NullPointerException.class, tftp::discardPackets);
            tftp.open();
            tftp.discardPackets();
        }
    }

    private void testDownload(final int mode, final File file) throws IOException {
        // Create our TFTP instance to handle the file transfer.
        try (TFTPClient tftp = new TFTPClient() {
            @Override
            protected void trace(final String direction, final TFTPPacket packet) {
                System.out.println(direction + " " + packet);
            }
        }) {
            tftp.open();
            tftp.setSoTimeout(2000);

            final File out = new File(SERVER_DIR, FILE_PREFIX + "download");

            // cleanup old failed runs
            out.delete();
            assertFalse("Couldn't clear output location", out.exists());

            try (FileOutputStream output = new FileOutputStream(out)) {
                tftp.receiveFile(file.getName(), mode, output, "localhost", SERVER_PORT);
            }

            assertTrue("file not created", out.exists());
            assertTrue("FILES not identical on file " + file, contentEquals(out, file));

            // delete the downloaded file
            out.delete();
        }
    }

    public void testGetModeName() {
        assertNotNull(TFTP.getModeName(0));
        assertNotNull(TFTP.getModeName(1));
    }

    public void testHugeDownloads() throws Exception {
        // test with the smaller FILES
        for (int i = 5; i < FILES.length; i++) {
            testDownload(TFTP.BINARY_MODE, FILES[i]);
        }
    }

    public void testHugeUploads() throws Exception {
        for (int i = 5; i < FILES.length; i++) {
            testUpload(TFTP.BINARY_MODE, FILES[i]);
        }
    }

    public void testSend() throws IOException {
        try (TFTP tftp = new TFTP()) {
            tftp.open();
            tftp.send(new TFTPDataPacket(InetAddress.getLocalHost(), tftp.getLocalPort(), 0, new byte[10]));
        }
    }

    public void testTFTPBinaryDownloads() throws Exception {
        // test with the smaller FILES
        for (int i = 0; i < 6; i++) {
            testDownload(TFTP.BINARY_MODE, FILES[i]);
        }
    }

    public void testTFTPBinaryUploads() throws Exception {
        // test with the smaller FILES
        for (int i = 0; i < 6; i++) {
            testUpload(TFTP.BINARY_MODE, FILES[i]);
        }
    }

    private void testUpload(final int mode, final File file) throws Exception {
        // Create our TFTP instance to handle the file transfer.
        try (TFTPClient tftp = new TFTPClient()) {
            tftp.open();
            tftp.setSoTimeout(2000);

            final File in = new File(SERVER_DIR, FILE_PREFIX + "upload");
            // cleanup old failed runs
            in.delete();
            assertFalse("Couldn't clear output location", in.exists());

            try (FileInputStream fis = new FileInputStream(file)) {
                tftp.sendFile(in.getName(), mode, fis, "localhost", SERVER_PORT);
            }

            // need to give the server a bit of time to receive our last packet, and
            // close out its file buffers, etc.
            Thread.sleep(100);
            assertTrue("file not created", in.exists());
            assertTrue("FILES not identical on file " + file, contentEquals(file, in));

            in.delete();
        }
    }
}
