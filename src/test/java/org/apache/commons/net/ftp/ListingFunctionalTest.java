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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A functional test suite for checking that site listings work.
 */
public class ListingFunctionalTest extends TestCase {
    // Offsets within testData below
    static final int HOSTNAME = 0;
    static final int VALID_PARSERKEY = 1;
    static final int INVALID_PARSERKEY = 2;
    static final int INVALID_PATH = 3;
    static final int VALID_FILENAME = 4;
    static final int VALID_PATH = 5;
    static final int PATH_PWD = 6; // response to PWD

    public static final Test suite() {
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
        final Class<?> clasz = ListingFunctionalTest.class;
        final Method[] methods = clasz.getDeclaredMethods();
        final TestSuite allSuites = new TestSuite("FTP Listing Functional Test Suite");

        for (final String[] element : testData) {
            final TestSuite suite = new TestSuite(element[VALID_PARSERKEY] + " @ " + element[HOSTNAME]);

            for (final Method method : methods) {
                if (method.getName().startsWith("test")) {
                    suite.addTest(new ListingFunctionalTest(method.getName(), element));
                }
            }

            allSuites.addTest(suite);
        }

        return allSuites;
    }

    private FTPClient client;
    private final String hostName;
    private final String invalidParserKey;
    private final String invalidPath;
    private final String validFilename;
    private final String validParserKey;
    private final String validPath;
    private final String pwdPath;

    public ListingFunctionalTest(final String arg0, final String[] settings) {
        super(arg0);
        invalidParserKey = settings[INVALID_PARSERKEY];
        validParserKey = settings[VALID_PARSERKEY];
        invalidPath = settings[INVALID_PATH];
        validFilename = settings[VALID_FILENAME];
        validPath = settings[VALID_PATH];
        pwdPath = settings[PATH_PWD];
        hostName = settings[HOSTNAME];
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

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new FTPClient();
        client.connect(hostName);
        client.login("anonymous", "anonymous");
        client.enterLocalPassiveMode();
//        client.addProtocolCommandListener(new PrintCommandListener(System.out));
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        try {
            client.logout();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (client.isConnected()) {
            client.disconnect();
        }

        client = null;
        super.tearDown();
    }

    /*
     * Test for FTPListParseEngine initiateListParsing()
     */
    public void testInitiateListParsing() throws IOException {
        client.changeWorkingDirectory(validPath);

        final FTPListParseEngine engine = client.initiateListParsing();
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    public void testInitiateListParsingWithPath() throws IOException {
        final FTPListParseEngine engine = client.initiateListParsing(validParserKey, validPath);
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    public void testInitiateListParsingWithPathAndAutodetection() throws IOException {
        final FTPListParseEngine engine = client.initiateListParsing(validPath);
        final List<FTPFile> files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    public void testInitiateListParsingWithPathAndAutodetectionButEmpty() throws IOException {
        final FTPListParseEngine engine = client.initiateListParsing(invalidPath);

        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    public void testInitiateListParsingWithPathAndIncorrectParser() throws IOException {
        final FTPListParseEngine engine = client.initiateListParsing(invalidParserKey, invalidPath);

        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFiles() throws IOException {
        final FTPClientConfig config = new FTPClientConfig(validParserKey);
        client.configure(config);
        final List<FTPFile> files = Arrays.asList(client.listFiles(validPath));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    public void testListFilesWithAutodection() throws IOException {
        client.changeWorkingDirectory(validPath);

        final List<FTPFile> files = Arrays.asList(client.listFiles());

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFilesWithIncorrectParser() throws IOException {
        final FTPClientConfig config = new FTPClientConfig(invalidParserKey);
        client.configure(config);

        final FTPFile[] files = client.listFiles(validPath);

        assertNotNull(files);

        // This may well fail, e.g. window parser for VMS listing
        assertArrayEquals("Expected empty array: " + Arrays.toString(files), new FTPFile[] {}, files);
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    public void testListFilesWithPathAndAutodectionButEmpty() throws IOException {
        final FTPFile[] files = client.listFiles(invalidPath);

        assertEquals(0, files.length);
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    public void testListFilesWithPathAndAutodetection() throws IOException {
        final List<FTPFile> files = Arrays.asList(client.listFiles(validPath));

        assertTrue(files.toString(), findByName(files, validFilename));
    }

    /*
     * Test for String[] listNames()
     */
    public void testListNames() throws IOException {
        client.changeWorkingDirectory(validPath);

        final String[] names = client.listNames();

        assertNotNull(names);

        final List<String> lnames = Arrays.asList(names);

        assertTrue(lnames.toString(), lnames.contains(validFilename));
    }

    /*
     * Test for String[] listNames(String)
     */
    public void testListNamesWithPath() throws IOException {
        final String[] listNames = client.listNames(validPath);
        assertNotNull("listNames not null", listNames);
        final List<String> names = Arrays.asList(listNames);

        assertTrue(names.toString(), findByName(names, validFilename));
    }

    public void testListNamesWithPathButEmpty() throws IOException {
        final String[] names = client.listNames(invalidPath);

        assertNull(names);
    }

    public void testPrintWorkingDirectory() throws IOException {
        client.changeWorkingDirectory(validPath);
        final String pwd = client.printWorkingDirectory();
        assertEquals(pwdPath, pwd);
    }
}
