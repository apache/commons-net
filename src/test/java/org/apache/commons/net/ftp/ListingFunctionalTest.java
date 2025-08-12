/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.PrintCommandListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A functional test suite for checking that site listings work.
 */
public class ListingFunctionalTest {
    // Offsets within testData below
    static final int HOSTNAME = 0;
    static final int VALID_PARSERKEY = 1;
    static final int INVALID_PARSERKEY = 2;
    static final int INVALID_PATH = 3;
    static final int VALID_FILENAME = 4;
    static final int VALID_PATH = 5;
    static final int PATH_PWD = 6; // response to PWD

    public static final class TestCase {
        private final String hostName;
        private final String invalidParserKey;
        private final String invalidPath;
        private final String validFilename;
        private final String validParserKey;
        private final String validPath;
        private final String pwdPath;

        private TestCase(final String[] settings) {
            invalidParserKey = settings[INVALID_PARSERKEY];
            validParserKey = settings[VALID_PARSERKEY];
            invalidPath = settings[INVALID_PATH];
            validFilename = settings[VALID_FILENAME];
            validPath = settings[VALID_PATH];
            pwdPath = settings[PATH_PWD];
            hostName = settings[HOSTNAME];
        }

        @Override
        public String toString() {
            return validParserKey + " @ " + hostName;
        }
    }

    private static Stream<TestCase> testCases() {
        final String[][] testData = { { "ftp.ibiblio.org", "unix", "vms", "HA!", "javaio.jar", "pub/languages/java/javafaq", "/pub/languages/java/javafaq", },
                { "apache.cs.utah.edu", "unix", "vms", "HA!", "HEADER.html", "apache.org", "/apache.org", },
//                { // not available
//                    "ftp.wacom.com", "windows", "VMS", "HA!",
//                    "wacom97.zip", "pub\\drivers"
//                },
                { "ftp.decuslib.com", "vms", "windows", // VMS OpenVMS V8.3
                        "[.HA!]", "FREEWARE_SUBMISSION_INSTRUCTIONS.TXT;1", "[.FREEWAREV80.FREEWARE]", "DECUSLIB:[DECUS.FREEWAREV80.FREEWARE]" },
//                {  // VMS TCPware V5.7-2 does not return (RWED) permissions
//                    "ftp.process.com", "vms", "windows",
//                    "[.HA!]", "MESSAGE.;1",
//                    "[.VMS-FREEWARE.FREE-VMS]" //
//                },
        };
        return Arrays.stream(testData).map(TestCase::new);
    }

    private FTPClient client;

    private FTPClient createFTPClient(final String hostName) {
        try {
            final FTPClient ftpClient = new FTPClient();
            ftpClient.addProtocolCommandListener(new PrintCommandListener(System.out));
            ftpClient.connect(hostName);
            ftpClient.login("anonymous", "anonymous");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setAutodetectUTF8(true);
            ftpClient.opts("UTF-8", "NLST");
            return ftpClient;
        } catch (final SocketException e) {
            return fail("Could not connect to FTP", e);
        } catch (final IOException e) {
            return fail(e);
        }
    }

    private boolean findByName(final List<?> fileList, final String string) {
        boolean found = false;
        final Iterator<?> iter = fileList.iterator();
        while (iter.hasNext() && !found) {
            final Object element = iter.next();
            if (element instanceof FTPFile) {
                final FTPFile file = (FTPFile) element;
                found = file.getName().equals(string);
            } else {
                final String fileName = (String) element;
                found = fileName.endsWith(string);
            }
        }
        return found;
    }

    @AfterEach
    protected void tearDown() throws Exception {
        if (client == null) {
            return;
        }
        try {
            client.logout();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (client.isConnected()) {
            client.disconnect();
        }
        client = null;
    }

    /*
     * Test for FTPListParseEngine initiateListParsing()
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testInitiateListParsing(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        client.changeWorkingDirectory(testCase.validPath);
        final FTPListParseEngine engine = client.initiateListParsing();
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testInitiateListParsingWithPath(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPListParseEngine engine = client.initiateListParsing(testCase.validParserKey, testCase.validPath);
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testInitiateListParsingWithPathAndAutodetection(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPListParseEngine engine = client.initiateListParsing(testCase.validPath);
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testInitiateListParsingWithPathAndAutodetectionButEmpty(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPListParseEngine engine = client.initiateListParsing(testCase.invalidPath);
        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testInitiateListParsingWithPathAndIncorrectParser(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPListParseEngine engine = client.initiateListParsing(testCase.invalidParserKey, testCase.invalidPath);
        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListFiles(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPClientConfig config = new FTPClientConfig(testCase.validParserKey);
        client.configure(config);
        final List<FTPFile> files = Arrays.asList(client.listFiles(testCase.validPath));
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListFilesWithAutodection(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        client.changeWorkingDirectory(testCase.validPath);
        final List<FTPFile> files = Arrays.asList(client.listFiles());
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListFilesWithIncorrectParser(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPClientConfig config = new FTPClientConfig(testCase.invalidParserKey);
        client.configure(config);
        final FTPFile[] files = client.listFiles(testCase.validPath);
        assertNotNull(files);
        // This may well fail, e.g. window parser for VMS listing
        assertArrayEquals(new FTPFile[] {}, files, "Expected empty array: " + Arrays.toString(files));
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListFilesWithPathAndAutodectionButEmpty(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final FTPFile[] files = client.listFiles(testCase.invalidPath);
        assertEquals(0, files.length);
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListFilesWithPathAndAutodetection(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final List<FTPFile> files = Arrays.asList(client.listFiles(testCase.validPath));
        assertTrue(findByName(files, testCase.validFilename), files.toString());
    }

    /*
     * Test for String[] listNames()
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListNames(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        client.changeWorkingDirectory(testCase.validPath);
        final String[] names = client.listNames();
        assertNotNull(names);
        final List<String> lnames = Arrays.asList(names);
        assertTrue(lnames.contains(testCase.validFilename), lnames.toString());
    }

    /*
     * Test for String[] listNames(String)
     */
    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListNamesWithPath(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final String[] listNames = client.listNames(testCase.validPath);
        assertNotNull(listNames, "listNames not null");
        final List<String> names = Arrays.asList(listNames);
        assertTrue(findByName(names, testCase.validFilename), names.toString());
    }

    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testListNamesWithPathButEmpty(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        final String[] names = client.listNames(testCase.invalidPath);
        assertTrue(ArrayUtils.isEmpty(names));
    }

    @ParameterizedTest(name = "hostname={0}")
    @MethodSource("testCases")
    public void testPrintWorkingDirectory(final TestCase testCase) throws IOException {
        client = createFTPClient(testCase.hostName);
        client.changeWorkingDirectory(testCase.validPath);
        final String pwd = client.printWorkingDirectory();
        assertEquals(testCase.pwdPath, pwd);
    }
}
