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

import java.io.IOException;
import java.net.SocketException;
import java.time.Instant;
import java.util.Calendar;

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
public class FTPSClientTest extends AbstractFtpsTest {

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

    public FTPSClientTest(final boolean endpointCheckingEnabled) {
        super(endpointCheckingEnabled, null, null);
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
            client.disconnect();
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
            client.disconnect();
        }
    }

    @Test(timeout = TEST_TIMEOUT)
    public void testMdtmFile() throws SocketException, IOException {
        trace(">>testMdtmFile");
        testMdtmFile("/file.txt");
        trace("<<testMdtmFile");
    }

    private void testMdtmFile(final String pathname) throws SocketException, IOException {
        final FTPSClient client = loginClient();
        try {
            // do it twice
            final FTPFile mdtmFile1 = client.mdtmFile(pathname);
            final FTPFile mdtmFile2 = client.mdtmFile(pathname);
            assertNotNull(mdtmFile1);
            assertNotNull(mdtmFile2);
            assertEquals(mdtmFile1.toString(), mdtmFile2.toString());
        } finally {
            client.disconnect();
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
            client.disconnect();
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
            ftpsClient.disconnect();
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
