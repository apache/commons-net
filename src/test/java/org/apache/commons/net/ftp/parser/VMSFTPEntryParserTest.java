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
import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPListParseEngine;

/**
 */
public class VMSFTPEntryParserTest extends AbstractFTPParseTest {
    private static final String[] BAD_SAMPLES = {

            "1-JUN.LIS;2              9/9           JUN-2-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)",
            "1-JUN.LIS;2              a/9           2-JUN-98 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)",
            "DATA.DIR; 1              1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (,RWED,RWED,RE)",
            "120196.TXT;1           118/126        14-APR-1997 12:45:27 PM  [GROUP,OWNER]    (RWED,,RWED,RE)",
            "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP-1,OWNER]    (RWED,RWED,RWED,RE)",
            "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED2,RWED,RWED,RE)",
            "AA.;2                  152/153        13-FED-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", "Directory USER1:[TEMP]\r\n\r\n",
            "\r\nTotal 14 files" };

    // CHECKSTYLE:OFF (long lines)
    private static final String[] GOOD_SAMPLES = { "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
            "1-JUN.LIS;3              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)",
            "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)",
            "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [TRANSLATED]     (,RWED,RWED,RE)",
            "120196.TXT;1           118/126        14-APR-1997 12:45:27  [GROUP,OWNER]    (RWED,,RWED,RE)",
            "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
            "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
            "AA.;2                  152/153        13-FEB-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
            "UCX$REXECD_STARTUP.LOG;1098\r\n" + "                         4/15         24-FEB-2003 13:17:24  [POSTWARE,LP]    (RWED,RWED,RE,)",
            "UNARCHIVE.COM;1          2/15          7-JUL-1997 16:37:45  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
            "UNXMERGE.COM;15          1/15         20-AUG-1996 13:59:50  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
            "UNXTEMP.COM;7            1/15         15-AUG-1996 14:10:38  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
            "UNZIP_AND_ATTACH_FILES.COM;12\r\n" + "                        14/15         24-JUL-2002 14:35:40  [TRANSLATED]    (RWE,RWE,RWE,RE)",
            "UNZIP_AND_ATTACH_FILES.SAV;1\r\n" + "                        14/15         17-JAN-2002 11:13:53  [POSTWARE,LP]    (RWE,RWED,RWE,RE)",
            "FREEWARE40.DIR;1        27/36" + "         16-FEB-1999 10:01:46  [AP_HTTPD,APACHE$WWW                               (RWE,RWE,RE,RE)",
            "1-JUN.LIS;1              9/9           2-jun-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
            "ALLOCMISS.COM;1            1         15-AUG-1996 14:10:38  [POSTWARE,LP]    (RWE,RWE,RWE,RE)" };
    // CHECKSTYLE:ON

    private static final String FULL_LISTING = "Directory USER1:[TEMP]\r\n\r\n"
            + "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)\r\n"
            + "2-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"
            + "3-JUN.LIS;1              9/9           3-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"
            + "3-JUN.LIS;4              9/9           7-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"
            + "3-JUN.LIS;2              9/9           4-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"
            + "3-JUN.LIS;3              9/9           6-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n" + "\r\nTotal 6 files";

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public VMSFTPEntryParserTest(final String name) {
        super(name);
    }

    public void assertFileInListing(final FTPFile[] listing, final String name) {
        for (final FTPFile element : listing) {
            if (name.equals(element.getName())) {
                return;
            }
        }
        fail("File " + name + " not found in supplied listing");
    }

    public void assertFileNotInListing(final FTPFile[] listing, final String name) {
        for (final FTPFile element : listing) {
            if (name.equals(element.getName())) {
                fail("Unexpected File " + name + " found in supplied listing");
            }
        }
    }

    /*
     * Verify that the VMS parser does NOT set the permissions.
     */
    private void checkPermisions(final FTPFile dir, final int octalPerm) {
        int permMask = 1 << 8;
        assertEquals("Owner should not have read permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        permMask >>= 1;
        assertEquals("Owner should not have write permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        permMask >>= 1;
        assertEquals("Owner should not have execute permission.", (permMask & octalPerm) != 0,
                dir.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        permMask >>= 1;
        assertEquals("Group should not have read permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        permMask >>= 1;
        assertEquals("Group should not have write permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        permMask >>= 1;
        assertEquals("Group should not have execute permission.", (permMask & octalPerm) != 0,
                dir.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        permMask >>= 1;
        assertEquals("World should not have read permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        permMask >>= 1;
        assertEquals("World should not have write permission.", (permMask & octalPerm) != 0, dir.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        permMask >>= 1;
        assertEquals("World should not have execute permission.", (permMask & octalPerm) != 0,
                dir.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Override
    protected String[] getBadListing() {

        return BAD_SAMPLES;
    }

    @Override
    protected String[] getGoodListing() {

        return GOOD_SAMPLES;
    }

    @Override
    protected FTPFileEntryParser getParser() {
        final ConfigurableFTPFileEntryParserImpl parser = new VMSFTPEntryParser();
        parser.configure(null);
        return parser;
    }

    protected FTPFileEntryParser getVersioningParser() {
        final ConfigurableFTPFileEntryParserImpl parser = new VMSVersioningFTPEntryParser();
        parser.configure(null);
        return parser;
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [TRANSLATED]    (RWED,RD,,)", CalendarUnit.SECOND);
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {

        FTPFile dir = getParser().parseFTPEntry("DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a directory.", dir.isDirectory());
        assertEquals("DATA.DIR", dir.getName());
        assertEquals(512, dir.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", df.format(dir.getTimestamp().getTime()));
        assertEquals("GROUP", dir.getGroup());
        assertEquals("OWNER", dir.getUser());
        checkPermisions(dir, 0775);

        dir = getParser().parseFTPEntry("DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [TRANSLATED]    (RWED,RWED,,RE)");
        assertTrue("Should be a directory.", dir.isDirectory());
        assertEquals("DATA.DIR", dir.getName());
        assertEquals(512, dir.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", df.format(dir.getTimestamp().getTime()));
        assertNull(dir.getGroup());
        assertEquals("TRANSLATED", dir.getUser());
        checkPermisions(dir, 0705);
    }

    @Override
    public void testParseFieldsOnFile() throws Exception {
        FTPFile file = getParser().parseFTPEntry("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RW,R)");
        assertTrue("Should be a file.", file.isFile());
        assertEquals("1-JUN.LIS", file.getName());
        assertEquals(9 * 512, file.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", df.format(file.getTimestamp().getTime()));
        assertEquals("GROUP", file.getGroup());
        assertEquals("OWNER", file.getUser());
        checkPermisions(file, 0764);

        file = getParser().parseFTPEntry("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [TRANSLATED]    (RWED,RD,,)");
        assertTrue("Should be a file.", file.isFile());
        assertEquals("1-JUN.LIS", file.getName());
        assertEquals(9 * 512, file.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", df.format(file.getTimestamp().getTime()));
        assertNull(file.getGroup());
        assertEquals("TRANSLATED", file.getUser());
        checkPermisions(file, 0400);
    }

    @Override
    public void testRecentPrecision() {
        // Not used
    }

    public void testWholeListParse() throws IOException {
        final VMSFTPEntryParser parser = new VMSFTPEntryParser();
        parser.configure(null);
        final FTPListParseEngine engine = new FTPListParseEngine(parser);
        engine.readServerList(new ByteArrayInputStream(FULL_LISTING.getBytes()), null); // use default encoding
        final FTPFile[] files = engine.getFiles();
        assertEquals(6, files.length);
        assertFileInListing(files, "2-JUN.LIS");
        assertFileInListing(files, "3-JUN.LIS");
        assertFileInListing(files, "1-JUN.LIS");
        assertFileNotInListing(files, "1-JUN.LIS;1");

    }

    public void testWholeListParseWithVersioning() throws IOException {

        final VMSFTPEntryParser parser = new VMSVersioningFTPEntryParser();
        parser.configure(null);
        final FTPListParseEngine engine = new FTPListParseEngine(parser);
        engine.readServerList(new ByteArrayInputStream(FULL_LISTING.getBytes()), null); // use default encoding
        final FTPFile[] files = engine.getFiles();
        assertEquals(3, files.length);
        assertFileInListing(files, "1-JUN.LIS;1");
        assertFileInListing(files, "2-JUN.LIS;1");
        assertFileInListing(files, "3-JUN.LIS;4");
        assertFileNotInListing(files, "3-JUN.LIS;1");
        assertFileNotInListing(files, "3-JUN.LIS");

    }
}
