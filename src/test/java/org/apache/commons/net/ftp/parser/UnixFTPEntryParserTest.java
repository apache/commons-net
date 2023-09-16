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

import java.util.Calendar;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 */
public class UnixFTPEntryParserTest extends AbstractFTPParseTest {

    private static final String[] badsamples = { "zrwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox",
            "dxrwr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc", "drwxr-xr-x   2 root     root         4096 Jam  4 00:03 zziplib",
            "drwxr-xr-x   2 root     99           4096 Feb 23 30:01 zzplayer", "drwxr-xr-x   2 root     root         4096 Aug 36  2001 zztpp",
            "-rw-r--r--   1 14       staff       80284 Aug 22  zxJDBC-1.2.3.tar.gz", "-rw-r--r--   1 14       staff      119:26 Aug 22  2000 zxJDBC-1.2.3.zip",
            /* "-rw-r--r--   1 ftp      no group    83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz", */
            "-rw-r--r--   1ftp       nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
            "-rw-r--r--   1 root     root       190144 2001-04-27 zxJDBC-2.0.1b1.zip",
            "-rw-r--r--   1 root     root       111325 Apr -7 18:79 zxJDBC-2.0.1b1.tar.gz" };

    private static final String[] goodsamples = { "-rw-r--r--   1 500      500            21 Aug  8 14:14 JB3-TES1.gz",
            "-rwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox", "drwxr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc",
            "drwxr-xr-x   2 root     root         4096 Jan  4 00:03 zziplib", "drwxr-xr-x   2 root     99           4096 Feb 23  2001 zzplayer",
            "drwxr-xr-x   2 root     root         4096 Aug  6  2001 zztpp", "drwxr-xr-x 1 usernameftp 512 Jan 29 23:32 prog",
            "lrw-r--r--   1 14       14          80284 Aug 22  2000 zxJDBC-1.2.3.tar.gz",
            "frw-r--r--   1 14       staff      119926 Aug 22  2000 zxJDBC-1.2.3.zip",
            "crw-r--r--   1 ftp      nogroup     83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
            "brw-r--r--   1 ftp      nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
            "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz",
            "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip",
            "-rwxr-xr-x   2 500      500           166 Nov  2  2001 73131-testtes1.afp",
            "-rw-r--r--   1 500      500           166 Nov  9  2001 73131-testtes1.AFP",
            "-rw-r--r--   1 500      500           166 Nov 12  2001 73131-testtes2.afp",
            "-rw-r--r--   1 500      500           166 Nov 12  2001 73131-testtes2.AFP",
            "-rw-r--r--   1 500      500       2040000 Aug  5 07:35 testRemoteUPCopyNIX",
            "-rw-r--r--   1 500      500       2040000 Aug  5 07:31 testRemoteUPDCopyNIX",
            "-rw-r--r--   1 500      500       2040000 Aug  5 07:31 testRemoteUPVCopyNIX", "-rw-r--r-T   1 500      500             0 Mar 25 08:20 testSticky",
            "-rwxr-xr-t   1 500      500             0 Mar 25 08:21 testStickyExec", "-rwSr-Sr--   1 500      500             0 Mar 25 08:22 testSuid",
            "-rwsr-sr--   1 500      500             0 Mar 25 08:23 testSuidExec", "-rwsr-sr--   1 500      500             0 Mar 25 0:23 testSuidExec2",
            "drwxrwx---+ 23 500     500    0 Jan 10 13:09 testACL", "-rw-r--r--   1 1        3518644 May 25 12:12 std",
            "lrwxrwxrwx   1 neeme neeme             23 Mar  2 18:06 macros -> ./../../global/macros/.",
            "-rw-r--r--   1 ftp      group with spaces in it as allowed in cygwin see bug 38634" + "   83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
            // Bug 38634 => NET-16
            "crw-r----- 1 root kmem 0, 27 Jan 30 11:42 kmem", // FreeBSD device
            "crw-------   1 root     sys      109,767 Jul  2  2004 pci@1c,600000:devctl", // Solaris device
            "-rwxrwx---   1 ftp      ftp-admin 816026400 Oct  5  2008 bloplab 7 cd1.img", // NET-294

            // https://mail-archives.apache.org/mod_mbox/commons-dev/200408.mbox/%3c4122F3C1.9090402@tanukisoftware.com%3e
            "-rw-r--r-- 1 1 3518644 May 25 12:12 std", "-rw-rw---- 1 ep1adm sapsys 0 6\u6708 3\u65e5 2003\u5e74 \u8a66\u9a13\u30d5\u30a1\u30a4\u30eb.csv",
            "-rw-rw---- 1 ep1adm sapsys 0 8\u6708 17\u65e5 20:10 caerrinf",

    };

    public UnixFTPEntryParserTest(final String name) {
        super(name);
    }

    private void checkPermissions(final FTPFile f) {
        assertTrue("Should have user read permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have user execute permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have group write permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have group execute permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have world read permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have world write permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have world execute permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Override
    protected void doAdditionalGoodTests(final String test, final FTPFile f) {
        final String link = f.getLink();
        if (null != link) {
            final int linklen = link.length();
            if (linklen > 0) {
                assertEquals(link, test.substring(test.length() - linklen));
                assertEquals(f.getType(), FTPFile.SYMBOLIC_LINK_TYPE);
            }
        }
        final int type = f.getType();
        switch (test.charAt(0)) {
        case 'd':
            assertEquals("Type of " + test, type, FTPFile.DIRECTORY_TYPE);
            break;
        case 'l':
            assertEquals("Type of " + test, type, FTPFile.SYMBOLIC_LINK_TYPE);
            break;
        case 'b':
        case 'c':
            assertEquals(0, f.getHardLinkCount());
            //$FALL-THROUGH$ TODO this needs to be fixed if a device type is introduced
        case 'f':
        case '-':
            assertEquals("Type of " + test, type, FTPFile.FILE_TYPE);
            break;
        default:
            assertEquals("Type of " + test, type, FTPFile.UNKNOWN_TYPE);
        }

        for (int access = FTPFile.USER_ACCESS; access <= FTPFile.WORLD_ACCESS; access++) {
            for (int perm = FTPFile.READ_PERMISSION; perm <= FTPFile.EXECUTE_PERMISSION; perm++) {
                final int pos = 3 * access + perm + 1;
                final char permchar = test.charAt(pos);
                assertEquals("Permission " + test.substring(1, 10), Boolean.valueOf(f.hasPermission(access, perm)),
                        Boolean.valueOf(permchar != '-' && !Character.isUpperCase(permchar)));
            }
        }

        assertNotNull("Expected to find a timestamp", f.getTimestamp());
// Perhaps check date range (need to ensure all good examples qualify)
//        assertTrue(test,f.getTimestamp().get(Calendar.YEAR)>=2000);
    }

    @Override
    protected String[] getBadListing() {
        return badsamples;
    }

    @Override
    protected String[] getGoodListing() {
        return goodsamples;
    }

    @Override
    protected FTPFileEntryParser getParser() {
        return new UnixFTPEntryParser();
    }

    public void testCorrectGroupNameParsing() {
        final FTPFile f = getParser().parseFTPEntry("-rw-r--r--   1 ftpuser  ftpusers 12414535 Mar 17 11:07 test 1999 abc.pdf");
        assertNotNull(f);
        assertEquals(1, f.getHardLinkCount());
        assertEquals("ftpuser", f.getUser());
        assertEquals("ftpusers", f.getGroup());
        assertEquals(12414535, f.getSize());
        assertEquals("test 1999 abc.pdf", f.getName());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 17);
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 7);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        assertEquals(f.getTimestamp().get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(f.getTimestamp().get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(f.getTimestamp().get(Calendar.HOUR_OF_DAY), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(f.getTimestamp().get(Calendar.MINUTE), cal.get(Calendar.MINUTE));
        assertEquals(f.getTimestamp().get(Calendar.SECOND), cal.get(Calendar.SECOND));
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("drwxr-xr-x   2 user     group         4096 Mar  2 2014 zxbox", CalendarUnit.DAY_OF_MONTH);
    }

    public void testFilenamesWithEmbeddedNumbers() {
        final FTPFile f = getParser().parseFTPEntry("-rw-rw-rw-   1 user group 5840 Mar 19 09:34 123 456 abc.csv");
        assertEquals("123 456 abc.csv", f.getName());
        assertEquals(5840, f.getSize());
        assertEquals("user", f.getUser());
        assertEquals("group", f.getGroup());
    }

    public void testGroupNameWithSpaces() {
        final FTPFile f = getParser().parseFTPEntry("drwx------ 4 maxm Domain Users 512 Oct 2 10:59 .metadata");
        assertNotNull(f);
        assertEquals("maxm", f.getUser());
        assertEquals("Domain Users", f.getGroup());
    }

    public void testLeadingSpacesDefault() { // the default has been changed to keep spaces
        final FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 john smith     group         4096 Mar  2 15:13   zxbox");
        assertNotNull(f);
        assertEquals("  zxbox", f.getName()); // leading spaces retained
    }

    public void testLeadingSpacesNET566() { // check new behavior
        final FTPFile f = new UnixFTPEntryParser(null, false).parseFTPEntry("drwxr-xr-x   2 john smith     group         4096 Mar  2 15:13   zxbox");
        assertNotNull(f);
        assertEquals("  zxbox", f.getName()); // leading spaces retained
    }

    public void testNameWIthPunctuation() {
        final FTPFile f = getParser().parseFTPEntry("drwx------ 4 maxm Domain Users 512 Oct 2 10:59 abc(test)123.pdf");
        assertNotNull(f);
        assertEquals("abc(test)123.pdf", f.getName());
    }

    public void testNET294() {
        final FTPFile f = getParser().parseFTPEntry("-rwxrwx---   1 ftp      ftp-admin 816026400 Oct  5  2008 bloplab 7 cd1.img");
        assertNotNull(f);
        assertEquals("ftp", f.getUser());
        assertEquals("ftp-admin", f.getGroup());
        assertEquals(816026400L, f.getSize());
        assertNotNull("Timestamp should not be null", f.getTimestamp());
        assertEquals(2008, f.getTimestamp().get(Calendar.YEAR));
        assertEquals("bloplab 7 cd1.img", f.getName());
    }

    public void testNoSpacesBeforeFileSize() {
        final FTPFile f = getParser().parseFTPEntry("drwxr-x---+1464 chrism   chrism     41472 Feb 25 13:17 20090225");
        assertNotNull(f);
        assertEquals(41472, f.getSize());
        assertEquals(f.getType(), FTPFile.DIRECTORY_TYPE);
        assertEquals("chrism", f.getUser());
        assertEquals("chrism", f.getGroup());
        assertEquals(1464, f.getHardLinkCount());
    }

    public void testNumericDateFormat() {
        final String testNumericDF = "-rw-r-----   1 neeme neeme   346 2005-04-08 11:22 services.vsp";
        final String testNumericDF2 = "lrwxrwxrwx   1 neeme neeme    23 2005-03-02 18:06 macros -> ./../../global/macros/.";

        final UnixFTPEntryParser parser = new UnixFTPEntryParser(UnixFTPEntryParser.NUMERIC_DATE_CONFIG);

        final FTPFile f = parser.parseFTPEntry(testNumericDF);
        assertNotNull("Failed to parse " + testNumericDF, f);

        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2005);
        cal.set(Calendar.MONTH, Calendar.APRIL);

        cal.set(Calendar.DAY_OF_MONTH, 8);
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 22);
        assertEquals(cal.getTime(), f.getTimestamp().getTime());

        final FTPFile f2 = parser.parseFTPEntry(testNumericDF2);
        assertNotNull("Failed to parse " + testNumericDF2, f2);
        assertEquals("symbolic link", "./../../global/macros/.", f2.getLink());

    }

    public void testOwnerAndGroupNameWithSpaces() {
        final FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 john smith     test group         4096 Mar  2 15:13 zxbox");
        assertNotNull(f);
        assertEquals("john smith", f.getUser());
        assertEquals("test group", f.getGroup());
    }

    public void testOwnerNameWithSpaces() {
        final FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 john smith     group         4096 Mar  2 15:13 zxbox");
        assertNotNull(f);
        assertEquals("john smith", f.getUser());
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        final FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 user     group         4096 Mar  2 15:13 zxbox");
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a directory.", f.isDirectory());
        checkPermissions(f);
        assertEquals(2, f.getHardLinkCount());
        assertEquals("user", f.getUser());
        assertEquals("group", f.getGroup());
        assertEquals("zxbox", f.getName());
        assertEquals(4096, f.getSize());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (f.getTimestamp().getTime().before(cal.getTime())) {
            cal.add(Calendar.YEAR, -1);
        }
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 13);

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    @Override
    public void testParseFieldsOnFile() throws Exception {
        final FTPFile f = getParser().parseFTPEntry("-rwxr-xr-x   2 user     my group 500        5000000000 Mar  2 15:13 zxbox");
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a file.", f.isFile());
        checkPermissions(f);
        assertEquals(2, f.getHardLinkCount());
        assertEquals("user", f.getUser());
        assertEquals("my group 500", f.getGroup());
        assertEquals("zxbox", f.getName());
        assertEquals(5000000000L, f.getSize());

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (f.getTimestamp().getTime().before(cal.getTime())) {
            cal.add(Calendar.YEAR, -1);
        }
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 13);
        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    // https://mail-archives.apache.org/mod_mbox/commons-dev/200408.mbox/%3c4122F3C1.9090402@tanukisoftware.com%3e
    public void testParseFieldsOnFileJapaneseTime() {
        final FTPFile f = getParser().parseFTPEntry("-rwxr-xr-x 2 user group 4096 3\u6708 2\u65e5 15:13 zxbox");
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a file.", f.isFile());
        checkPermissions(f);
        assertEquals(2, f.getHardLinkCount());
        assertEquals("user", f.getUser());
        assertEquals("group", f.getGroup());
        assertEquals("zxbox", f.getName());
        assertEquals(4096, f.getSize());

        assertNotNull("Timestamp not null", f.getTimestamp());
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (f.getTimestamp().getTime().before(cal.getTime())) {
            cal.add(Calendar.YEAR, -1);
        }
        cal.set(Calendar.DATE, 2);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 13);
        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    // https://mail-archives.apache.org/mod_mbox/commons-dev/200408.mbox/%3c4122F3C1.9090402@tanukisoftware.com%3e
    public void testParseFieldsOnFileJapaneseYear() {
        final FTPFile f = getParser().parseFTPEntry("-rwxr-xr-x 2 user group 4096 3\u6708 2\u65e5 2003\u5e74 \u8a66\u9a13\u30d5\u30a1\u30a4\u30eb.csv");
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a file.", f.isFile());
        checkPermissions(f);
        assertEquals(2, f.getHardLinkCount());
        assertEquals("user", f.getUser());
        assertEquals("group", f.getGroup());
        assertEquals("\u8a66\u9a13\u30d5\u30a1\u30a4\u30eb.csv", f.getName());
        assertEquals(4096, f.getSize());

        assertNotNull("Timestamp not null", f.getTimestamp());
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2003);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DATE, 2);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    @Override
    public void testRecentPrecision() {
        testPrecision("drwxr-xr-x   2 user     group         4096 Mar  2 15:13 zxbox", CalendarUnit.MINUTE);
    }

    public void testTrailingSpaces() {
        final FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 john smith     group         4096 Mar  2 15:13 zxbox     ");
        assertNotNull(f);
        assertEquals("zxbox     ", f.getName());
    }

    public void testTrimLeadingSpacesNET566() { // check can trim spaces as before
        final FTPFile f = new UnixFTPEntryParser(null, true).parseFTPEntry("drwxr-xr-x   2 john smith     group         4096 Mar  2 15:13   zxbox");
        assertNotNull(f);
        assertEquals("zxbox", f.getName()); // leading spaces trimmed
    }
}
