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
package org.apache.commons.net.ftp.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.junit.jupiter.api.Test;

/**
 */
class OS400FTPEntryParserTest extends CompositeFTPParseTestFramework {
    private static final String[][] badsamples = {
            { "PEP              4019 04/03/18 18:58:16 STMF       einladung.zip", "PEP               422 03/24 14:06:26 *STMF      readme",
                    "PEP              6409 04/03/24 30:06:29 *STMF      build.xml", "PEP USR         36864 04/03/24 14:06:34 *DIR       dir1/",
                    "PEP             3686404/03/24 14:06:47 *DIR       zdir2/" },

            { "----rwxr-x   1PEP       0           4019 Mar 18 18:58 einladung.zip", "----rwxr-x   1 PEP      0  xx        422 Mar 24 14:06 readme",
                    "----rwxr-x   1 PEP      0           8492 Apr 07 30:13 build.xml", "d---rwxr-x   2 PEP      0          45056Mar 24 14:06 zdir2" } };

    private static final String[][] goodsamples = {
            { "PEP              4019 04/03/18 18:58:16 *STMF      einladung.zip", "PEP               422 04/03/24 14:06:26 *STMF      readme",
                    "PEP              6409 04/03/24 14:06:29 *STMF      build.xml", "PEP             36864 04/03/24 14:06:34 *DIR       dir1/",
                    "PEP             36864 04/03/24 14:06:47 *DIR       zdir2/" },
            { "----rwxr-x   1 PEP      0           4019 Mar 18 18:58 einladung.zip", "----rwxr-x   1 PEP      0            422 Mar 24 14:06 readme",
                    "----rwxr-x   1 PEP      0           8492 Apr 07 07:13 build.xml", "d---rwxr-x   2 PEP      0          45056 Mar 24 14:06 dir1",
                    "d---rwxr-x   2 PEP      0          45056 Mar 24 14:06 zdir2" } };

    @Override
    protected void doAdditionalGoodTests(final String test, final FTPFile f) {
        if (test.startsWith("d")) {
            assertEquals(FTPFile.DIRECTORY_TYPE, f.getType(), "directory.type");
        }
    }

    /**
     * @see AbstractFTPParseTest#getBadListing()
     */
    @Override
    protected String[][] getBadListings() {
        return badsamples;
    }

    /**
     * @see AbstractFTPParseTest#getGoodListing()
     */
    @Override
    protected String[][] getGoodListings() {
        return goodsamples;
    }

    /**
     * @see AbstractFTPParseTest#getParser()
     */
    @Override
    protected FTPFileEntryParser getParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[] { new OS400FTPEntryParser(), new UnixFTPEntryParser()});
    }

    @Override
    @Test
    void testDefaultPrecision() {
        testPrecision("PEP              4019 04/03/18 18:58:16 *STMF      einladung.zip", CalendarUnit.SECOND);
    }

    /**
     * Tries to reproduce a fuzzing failure.
     */
    @Test
    void testFuzz() throws IOException {
        final byte[] allBytes = Files.readAllBytes(
                Paths.get("src/main/resources/org/apache/commons/net/fuzzer/clusterfuzz-testcase-minimized-OS400FTPEntryParserFuzzer-4734635798495232"));
        final OS400FTPEntryParser parser = new OS400FTPEntryParser();
        parser.configure(null);
        final FTPListParseEngine engine = new FTPListParseEngine(parser);
        // FTPListParseEngine
        engine.readServerList(new ByteArrayInputStream(allBytes), null); // use default encoding
        final FTPFile[] files = engine.getFiles();
        assertEquals(0, files.length);
        // OS400FTPEntryParser
        final String string = new String(allBytes, StandardCharsets.UTF_8);
        assertTrue(parser.matches(string));
        assertNull(parser.parseFTPEntry(string));
    }

    @Test
    void testNET573() {
        final FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_AS400);
        conf.setDefaultDateFormatStr("MM/dd/yy HH:mm:ss");
        final FTPFileEntryParser parser = new OS400FTPEntryParser(conf);

        final FTPFile f = parser.parseFTPEntry("ZFTPDEV 9069 05/20/15 15:36:52 *STMF /DRV/AUDWRKSHET/AUDWRK0204232015114625.PDF");
        assertNotNull(f, "Could not parse entry.");
        assertNotNull(f.getTimestamp(), "Could not parse timestamp.");
        assertFalse(f.isDirectory(), "Should not have been a directory.");
        assertEquals("ZFTPDEV", f.getUser());
        assertEquals("AUDWRK0204232015114625.PDF", f.getName());
        assertEquals(9069, f.getSize());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2015);
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.DAY_OF_MONTH, 20);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 36);
        cal.set(Calendar.SECOND, 52);

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    /**
     * @see AbstractFTPParseTest#testParseFieldsOnDirectory()
     */
    @Override
    @Test
    void testParseFieldsOnDirectory() throws Exception {
        final FTPFile f = getParser().parseFTPEntry("PEP             36864 04/03/24 14:06:34 *DIR       dir1/");
        assertNotNull(f, "Could not parse entry.");
        assertTrue(f.isDirectory(), "Should have been a directory.");
        assertEquals("PEP", f.getUser());
        assertEquals("dir1", f.getName());
        assertEquals(36864, f.getSize());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);

        cal.set(Calendar.YEAR, 2004);
        cal.set(Calendar.DAY_OF_MONTH, 24);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 6);
        cal.set(Calendar.SECOND, 34);

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    /**
     * @see AbstractFTPParseTest#testParseFieldsOnFile()
     */
    @Override
    @Test
    void testParseFieldsOnFile() throws Exception {
        final FTPFile f = getParser().parseFTPEntry("PEP              5000000000 04/03/24 14:06:29 *STMF      build.xml");
        assertNotNull(f, "Could not parse entry.");
        assertTrue(f.isFile(), "Should have been a file.");
        assertEquals("PEP", f.getUser());
        assertEquals("build.xml", f.getName());
        assertEquals(5000000000L, f.getSize());

        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 24);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.YEAR, 2004);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 6);
        cal.set(Calendar.SECOND, 29);
        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    /**
     * Test file names with spaces.
     */
    @Test
    void testParseFileNameWithSpaces() {
        final FTPFile f = getParser().parseFTPEntry("MYUSER              3 06/12/21 12:00:00 *STMF      file with space.txt");
        assertNotNull(f, "Could not parse entry.");
        assertTrue(f.isFile(), "Should have been a file.");
        assertEquals("file with space.txt", f.getName());
    }

    @Override
    @Test
    void testRecentPrecision() {
        testPrecision("----rwxr-x   1 PEP      0           4019 Mar 18 18:58 einladung.zip", CalendarUnit.MINUTE);
    }

}
