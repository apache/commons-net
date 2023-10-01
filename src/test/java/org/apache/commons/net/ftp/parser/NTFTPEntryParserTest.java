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
package org.apache.commons.net.ftp.parser;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPListParseEngine;

/**
 */
public class NTFTPEntryParserTest extends CompositeFTPParseTestFramework {

    private static final String[][] goodsamples = { { // DOS-style tests
            "05-26-95  10:57AM               143712 $LDR$", "05-20-97  03:31PM                  681 .bash_history",
            "12-05-96  05:03PM       <DIR>          absoft2", "11-14-97  04:21PM                  953 AUDITOR3.INI",
            "05-22-97  08:08AM                  828 AUTOEXEC.BAK", "01-22-98  01:52PM                  795 AUTOEXEC.BAT",
            "05-13-97  01:46PM                  828 AUTOEXEC.DOS", "12-03-96  06:38AM                  403 AUTOTOOL.LOG",
            "12-03-96  06:38AM       <DIR>          123xyz", "01-20-97  03:48PM       <DIR>          bin", "05-26-1995  10:57AM               143712 $LDR$",
            // 24hr clock as used on Windows_CE
            "12-05-96  17:03         <DIR>          absoft2", "05-22-97  08:08                    828 AUTOEXEC.BAK",
            "01-01-98  05:00       <DIR>          Network", "01-01-98  05:00       <DIR>          StorageCard", "09-13-10  20:08       <DIR>          Recycled",
            "09-06-06  19:00                   69 desktop.ini", "09-13-10  13:08                   23 Control Panel.lnk",
            "09-13-10  13:08       <DIR>          My Documents", "09-13-10  13:08       <DIR>          Program Files",
            "09-13-10  13:08       <DIR>          Temp", "09-13-10  13:08       <DIR>          Windows", },
            { // Unix-style tests
                    "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz",
                    "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip",
                    "-rwxr-xr-x   2 500      500           166 Nov  2  2001 73131-testtes1.afp",
                    "-rw-r--r--   1 500      500           166 Nov  9  2001 73131-testtes1.AFP",
                    "drwx------ 4 maxm Domain Users 512 Oct 2 10:59 .metadata", } };

    private static final String[][] badsamples = { { // DOS-style tests
            "20-05-97  03:31PM                  681 .bash_history", "     0           DIR   05-19-97   12:56  local",
            "     0           DIR   05-12-97   16:52  Maintenance Desktop", },
            { // Unix-style tests
                    "drwxr-xr-x   2 root     99           4096Feb 23 30:01 zzplayer", } };

    private static final String directoryBeginningWithNumber = "12-03-96  06:38AM       <DIR>          123xyz";

    // byte -123 when read using ISO-8859-1 encoding becomes 0X85 line terminator
    private static final byte[] listFilesByteTrace = { 48, 57, 45, 48, 52, 45, 49, 51, 32, 32, 48, 53, 58, 53, 49, 80, 77, 32, 32, 32, 32, 32, 32, 32, 60, 68,
            73, 82, 62, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 97, 115, 112, 110, 101, 116, 95, 99, 108, 105, 101, 110, 116, 13, 10, // 1
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 53, 52, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            50, 32, 65, 95, 113, 117, 105, 99, 107, 95, 98, 114, 111, 119, 110, 95, 102, 111, 120, 95, 106, 117, 109, 112, 115, 95, 111, 118, 101, 114, 95, 116,
            104, 101, 95, 108, 97, 122, 121, 95, 100, 111, 103, 13, 10, // 2
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 49, 55, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            51, 32, 120, -127, -123, 121, 13, 10, // 3
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 49, 58, 52, 57, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            52, 32, -126, -28, -126, -83, -119, -51, -126, -52, -105, -84, -126, -22, -126, -51, -112, -30, -126, -90, -126, -72, -126, -75, -126, -60, -127,
            65, -126, -75, -126, -87, -126, -32, -126, -32, -126, -58, -126, -52, -112, -123, -126, -55, -126, -96, -126, -25, -126, -72, 46, 116, 120, 116, 13,
            10, // 4
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 52, 54, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            53, 32, -125, 76, -125, -125, -125, 98, -125, 86, -125, 116, -125, -115, -127, 91, -116, 118, -114, 90, -113, -111, 13, 10, // 5
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 52, 54, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            54, 32, -125, 76, -125, -125, -125, 98, -125, 86, -125, -123, -125, 116, -125, -115, -127, 91, -116, 118, -114, 90, -113, -111, 13, 10, // 6
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 49, 58, 52, 57, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            55, 32, -114, 79, -116, -38, -126, -52, -105, -25, 46, 116, 120, 116, 13, 10, // 7
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 49, 58, 52, 57, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            56, 32, -111, -66, -116, -10, -106, 93, 46, 116, 120, 116, 13, 10, // 8
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 53, 52, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
            57, 32, -113, -84, -106, -20, -106, -123, -114, 113, 13, 10, // 9
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 49, 58, 52, 57, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            48, 32, -119, -28, -109, 99, -118, -108, -114, -82, -119, -17, -114, -48, -120, -8, -112, -123, -108, 95, -117, -58, 46, 80, 68, 70, 13, 10, // 10
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 49, 49, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            49, 32, -112, -124, -99, -56, 46, 116, 120, 116, 13, 10, // 11
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 52, 51, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            50, 32, -117, -76, -116, -123, 13, 10, // 12
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 49, 50, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            51, 32, -114, -107, -111, -123, -108, 94, -104, 82, 13, 10, // 13
            48, 55, 45, 48, 51, 45, 49, 51, 32, 32, 48, 50, 58, 51, 53, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            52, 32, -112, -123, -117, -101, -126, -52, -116, -16, -126, -19, -126, -24, 46, 116, 120, 116, 13, 10, // 14
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 50, 58, 49, 50, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            53, 32, -114, -123, -117, -101, -112, -20, 13, 10, // 15
            48, 55, 45, 49, 55, 45, 49, 51, 32, 32, 48, 49, 58, 52, 57, 80, 77, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 49,
            54, 32, -107, -94, -112, -123, -106, 126, -126, -55, -107, -44, -126, -25, -126, -72, 46, 116, 120, 116, 13, 10 // 16
    };

    private static final int LISTFILE_COUNT = 16;

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public NTFTPEntryParserTest(final String name) {
        super(name);
    }

    @Override
    protected void doAdditionalGoodTests(final String test, final FTPFile f) {
        if (test.indexOf("<DIR>") >= 0) {
            assertEquals("directory.type", FTPFile.DIRECTORY_TYPE, f.getType());
        }
    }

    /**
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getBadListings()
     */
    @Override
    protected String[][] getBadListings() {
        return badsamples;
    }

    /**
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getGoodListings()
     */
    @Override
    protected String[][] getGoodListings() {
        return goodsamples;
    }

    /**
     * @see org.apache.commons.net.ftp.parser.AbstractFTPParseTest#getParser()
     */
    @Override
    protected FTPFileEntryParser getParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[] { new NTFTPEntryParser(), new UnixFTPEntryParser()

        });
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("05-26-1995  10:57AM               143712 $LDR$", CalendarUnit.MINUTE);
        testPrecision("05-22-97  08:08                    828 AUTOEXEC.BAK", CalendarUnit.MINUTE);
    }

    /*
     * test condition reported as bug 20259 - now NET-106. directory with name beginning with a numeric character was not parsing correctly
     */
    public void testDirectoryBeginningWithNumber() {
        final FTPFile f = getParser().parseFTPEntry(directoryBeginningWithNumber);
        assertEquals("name", "123xyz", f.getName());
    }

    public void testDirectoryBeginningWithNumberFollowedBySpaces() {
        FTPFile f = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123 xyz");
        assertEquals("name", "123 xyz", f.getName());
        f = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123 abc xyz");
        assertNotNull(f);
        assertEquals("name", "123 abc xyz", f.getName());
    }

    /*
     * Test that group names with embedded spaces can be handled correctly
     *
     */
    public void testGroupNameWithSpaces() {
        final FTPFile f = getParser().parseFTPEntry("drwx------ 4 maxm Domain Users 512 Oct 2 10:59 .metadata");
        assertNotNull(f);
        assertEquals("maxm", f.getUser());
        assertEquals("Domain Users", f.getGroup());
    }

    public void testNET339() {
        final FTPFile file = getParser().parseFTPEntry("05-22-97  12:08                  5000000000 10 years and under");
        assertNotNull("Could not parse entry", file);
        assertEquals("10 years and under", file.getName());
        assertEquals(5000000000L, file.getSize());
        Calendar timestamp = file.getTimestamp();
        assertNotNull("Could not parse time", timestamp);
        assertEquals("Thu May 22 12:08:00 1997", df.format(timestamp.getTime()));

        final FTPFile dir = getParser().parseFTPEntry("12-03-96  06:38       <DIR>           10 years and under");
        assertNotNull("Could not parse entry", dir);
        assertEquals("10 years and under", dir.getName());
        timestamp = dir.getTimestamp();
        assertNotNull("Could not parse time", timestamp);
        assertEquals("Tue Dec 03 06:38:00 1996", df.format(timestamp.getTime()));
    }

    public void testNET516() throws Exception { // problem where part of a multi-byte char gets converted to 0x85 = line term
        final int utf = testNET516("UTF-8");
        assertEquals(LISTFILE_COUNT, utf);
        final int ascii = testNET516("ASCII");
        assertEquals(LISTFILE_COUNT, ascii);
        final int iso8859_1 = testNET516("ISO-8859-1");
        assertEquals(LISTFILE_COUNT, iso8859_1);
    }

    private int testNET516(final String charset) throws Exception {
        final FTPFileEntryParser parser = new NTFTPEntryParser();
        final FTPListParseEngine engine = new FTPListParseEngine(parser);
        engine.readServerList(new ByteArrayInputStream(listFilesByteTrace), charset);
        final FTPFile[] ftpfiles = engine.getFiles();
        return ftpfiles.length;
    }

    /**
     * @see org.apache.commons.net.ftp.parser.AbstractFTPParseTest#testParseFieldsOnDirectory()
     */
    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        FTPFile dir = getParser().parseFTPEntry("12-05-96  05:03PM       <DIR>          absoft2");
        assertNotNull("Could not parse entry.", dir);
        assertEquals("Thu Dec 05 17:03:00 1996", df.format(dir.getTimestamp().getTime()));
        assertTrue("Should have been a directory.", dir.isDirectory());
        assertEquals("absoft2", dir.getName());
        assertEquals(0, dir.getSize());

        dir = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123456");
        assertNotNull("Could not parse entry.", dir);
        assertTrue("Should have been a directory.", dir.isDirectory());
        assertEquals("123456", dir.getName());
        assertEquals(0, dir.getSize());

    }

    /**
     * @see org.apache.commons.net.ftp.parser.AbstractFTPParseTest#testParseFieldsOnFile()
     */
    @Override
    public void testParseFieldsOnFile() throws Exception {
        FTPFile f = getParser().parseFTPEntry("05-22-97  12:08AM                  5000000000 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", f);
        assertEquals("Thu May 22 00:08:00 1997", df.format(f.getTimestamp().getTime()));
        assertTrue("Should have been a file.", f.isFile());
        assertEquals("AUTOEXEC.BAK", f.getName());
        assertEquals(5000000000L, f.getSize());

        // test an NT-unix style listing that does NOT have a leading zero
        // on the hour.

        f = getParser().parseFTPEntry("-rw-rw-r--   1 mqm        mqm          17707 Mar 12  3:33 killmq.sh.log");
        assertNotNull("Could not parse entry.", f);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(f.getTimestamp().getTime());
        assertEquals("hour", 3, cal.get(Calendar.HOUR));
        assertTrue("Should have been a file.", f.isFile());
        assertEquals(17707, f.getSize());
    }

    public void testParseLeadingDigits() {
        final FTPFile file = getParser().parseFTPEntry("05-22-97  12:08AM                  5000000000 10 years and under");
        assertNotNull("Could not parse entry", file);
        assertEquals("10 years and under", file.getName());
        assertEquals(5000000000L, file.getSize());
        Calendar timestamp = file.getTimestamp();
        assertNotNull("Could not parse time", timestamp);
        assertEquals("Thu May 22 00:08:00 1997", df.format(timestamp.getTime()));

        final FTPFile dir = getParser().parseFTPEntry("12-03-96  06:38PM       <DIR>           10 years and under");
        assertNotNull("Could not parse entry", dir);
        assertEquals("10 years and under", dir.getName());
        timestamp = dir.getTimestamp();
        assertNotNull("Could not parse time", timestamp);
        assertEquals("Tue Dec 03 18:38:00 1996", df.format(timestamp.getTime()));
    }

    @Override
    public void testRecentPrecision() {
        // Not used
    }
}
