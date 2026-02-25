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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.time.Instant;
import java.util.Calendar;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
class FTPSClientTest extends AbstractFtpsTest {

    private static final String USER_PROPS_RES = "org/apache/commons/net/ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org/apache/commons/net/ftpsserver/ftpserver.jks";

    private static Stream<Boolean> endpointCheckingEnabledSource() {
        return Stream.of(Boolean.FALSE, Boolean.TRUE);
    }

    @BeforeAll
    public static void setupServer() throws Exception {
        setupServer(IMPLICIT, USER_PROPS_RES, SERVER_JKS_RES, "target/test-classes/org/apache/commons/net/test-data");
    }

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testHasFeature(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
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

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testListFilesPathNameEmpty(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
        trace(">>testListFilesPathNameEmpty");
        testListFiles("");
        trace("<<testListFilesPathNameEmpty");
    }

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testListFilesPathNameJunk(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
        trace(">>testListFilesPathNameJunk");
        testListFiles("   Junk   ");
        trace("<<testListFilesPathNameJunk");
    }

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testListFilesPathNameNull(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
        trace(">>testListFilesPathNameNull");
        testListFiles(null);
        trace("<<testListFilesPathNameNull");
    }

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testListFilesPathNameRoot(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
        trace(">>testListFilesPathNameRoot");
        testListFiles("/");
        trace("<<testListFilesPathNameRoot");
    }

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testMdtmCalendar(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
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

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testMdtmFile(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
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

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testMdtmInstant(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
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

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testOpenClose(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
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

    @ParameterizedTest(name = "endpointCheckingEnabled={0}")
    @MethodSource("endpointCheckingEnabledSource")
    @Timeout(TEST_TIMEOUT)
    void testRetrieveFilePathNameRoot(final boolean endpointCheckingEnabled) throws SocketException, IOException {
        setEndpointCheckingEnabled(endpointCheckingEnabled);
        trace(">>testRetrieveFilePathNameRoot");
        retrieveFile("/file.txt");
        trace("<<testRetrieveFilePathNameRoot");
    }

}
