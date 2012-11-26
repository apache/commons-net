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
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
 */
public class NTFTPEntryParserTest extends CompositeFTPParseTestFramework
{

    private static final String [][] goodsamples = {
    { // DOS-style tests
            "05-26-95  10:57AM               143712 $LDR$",
            "05-20-97  03:31PM                  681 .bash_history",
            "12-05-96  05:03PM       <DIR>          absoft2",
            "11-14-97  04:21PM                  953 AUDITOR3.INI",
            "05-22-97  08:08AM                  828 AUTOEXEC.BAK",
            "01-22-98  01:52PM                  795 AUTOEXEC.BAT",
            "05-13-97  01:46PM                  828 AUTOEXEC.DOS",
            "12-03-96  06:38AM                  403 AUTOTOOL.LOG",
            "12-03-96  06:38AM       <DIR>          123xyz",
            "01-20-97  03:48PM       <DIR>          bin",
            "05-26-1995  10:57AM               143712 $LDR$",
            // 24hr clock as used on Windows_CE
            "12-05-96  17:03         <DIR>          absoft2",
            "05-22-97  08:08                    828 AUTOEXEC.BAK",
            "01-01-98  05:00       <DIR>          Network",
            "01-01-98  05:00       <DIR>          StorageCard",
            "09-13-10  20:08       <DIR>          Recycled",
            "09-06-06  19:00                   69 desktop.ini",
            "09-13-10  13:08                   23 Control Panel.lnk",
            "09-13-10  13:08       <DIR>          My Documents",
            "09-13-10  13:08       <DIR>          Program Files",
            "09-13-10  13:08       <DIR>          Temp",
            "09-13-10  13:08       <DIR>          Windows",
    },
    { // Unix-style tests
            "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz",
            "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip",
            "-rwxr-xr-x   2 500      500           166 Nov  2  2001 73131-testtes1.afp",
            "-rw-r--r--   1 500      500           166 Nov  9  2001 73131-testtes1.AFP",
            "drwx------ 4 maxm Domain Users 512 Oct 2 10:59 .metadata",
        }
    };

    private static final String[][] badsamples =
        {
            { // DOS-style tests
                "20-05-97  03:31PM                  681 .bash_history",
                "     0           DIR   05-19-97   12:56  local",
                "     0           DIR   05-12-97   16:52  Maintenance Desktop",
            },
            { // Unix-style tests
                "drwxr-xr-x   2 root     99           4096Feb 23 30:01 zzplayer",
            }
            };

    private static final String directoryBeginningWithNumber =
        "12-03-96  06:38AM       <DIR>          123xyz";


    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public NTFTPEntryParserTest (String name)
    {
        super(name);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getGoodListings()
     */
    @Override
    protected String[][] getGoodListings()
    {
        return goodsamples;
    }

    /**
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getBadListings()
     */
    @Override
    protected String[][] getBadListings()
    {
        return badsamples;
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
     */
    @Override
    protected FTPFileEntryParser getParser()
    {
       return new CompositeFileEntryParser(new FTPFileEntryParser[]
        {
            new NTFTPEntryParser(),
            new UnixFTPEntryParser()

        });
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    @Override
    public void testParseFieldsOnDirectory() throws Exception
    {
        FTPFile dir = getParser().parseFTPEntry("12-05-96  05:03PM       <DIR>          absoft2");
        assertNotNull("Could not parse entry.", dir);
        assertEquals("Thu Dec 05 17:03:00 1996",
                     df.format(dir.getTimestamp().getTime()));
        assertTrue("Should have been a directory.",
                   dir.isDirectory());
        assertEquals("absoft2", dir.getName());
        assertEquals(0, dir.getSize());

        dir = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123456");
        assertNotNull("Could not parse entry.", dir);
        assertTrue("Should have been a directory.",
                dir.isDirectory());
        assertEquals("123456", dir.getName());
        assertEquals(0, dir.getSize());

    }

    public void testParseLeadingDigits() {
            FTPFile file = getParser().parseFTPEntry("05-22-97  12:08AM                  5000000000 10 years and under");
            assertNotNull("Could not parse entry", file);
            assertEquals("10 years and under", file.getName());
            assertEquals(5000000000L, file.getSize());
            Calendar timestamp = file.getTimestamp();
            assertNotNull("Could not parse time",timestamp);
            assertEquals("Thu May 22 00:08:00 1997",df.format(timestamp.getTime()));

            FTPFile dir = getParser().parseFTPEntry("12-03-96  06:38PM       <DIR>           10 years and under");
            assertNotNull("Could not parse entry", dir);
            assertEquals("10 years and under", dir.getName());
            timestamp = dir.getTimestamp();
            assertNotNull("Could not parse time",timestamp);
            assertEquals("Tue Dec 03 18:38:00 1996",df.format(timestamp.getTime()));
    }

    public void testNET339() { // TODO enable when NET-339 is fixed
        FTPFile file = getParser().parseFTPEntry("05-22-97  12:08                  5000000000 10 years and under");
        assertNotNull("Could not parse entry", file);
        assertEquals("10 years and under", file.getName());
        assertEquals(5000000000L, file.getSize());
        Calendar timestamp = file.getTimestamp();
        assertNotNull("Could not parse time",timestamp);
        assertEquals("Thu May 22 12:08:00 1997",df.format(timestamp.getTime()));

        FTPFile dir = getParser().parseFTPEntry("12-03-96  06:38       <DIR>           10 years and under");
        assertNotNull("Could not parse entry", dir);
        assertEquals("10 years and under", dir.getName());
        timestamp = dir.getTimestamp();
        assertNotNull("Could not parse time",timestamp);
        assertEquals("Tue Dec 03 06:38:00 1996",df.format(timestamp.getTime()));
}

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    @Override
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("05-22-97  12:08AM                  5000000000 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", f);
        assertEquals("Thu May 22 00:08:00 1997",
                     df.format(f.getTimestamp().getTime()));
        assertTrue("Should have been a file.",
                   f.isFile());
        assertEquals("AUTOEXEC.BAK", f.getName());
        assertEquals(5000000000L, f.getSize());

        // test an NT-unix style listing that does NOT have a leading zero
        // on the hour.

        f = getParser().parseFTPEntry(
                "-rw-rw-r--   1 mqm        mqm          17707 Mar 12  3:33 killmq.sh.log");
        assertNotNull("Could not parse entry.", f);
        Calendar cal = Calendar.getInstance();
        cal.setTime(f.getTimestamp().getTime());
        assertEquals("hour", 3, cal.get(Calendar.HOUR));
        assertTrue("Should have been a file.",
                f.isFile());
        assertEquals(17707, f.getSize());
    }


    @Override
    protected void doAdditionalGoodTests(String test, FTPFile f)
    {
        if (test.indexOf("<DIR>") >= 0)
        {
                    assertEquals("directory.type",
                            FTPFile.DIRECTORY_TYPE, f.getType());
        }
    }

    /**
     * test condition reported as bug 20259 => NET-106.
     * directory with name beginning with a numeric character
     * was not parsing correctly
     *
     * @throws Exception
     */
    public void testDirectoryBeginningWithNumber() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry(directoryBeginningWithNumber);
        assertEquals("name", "123xyz", f.getName());
    }

    public void testDirectoryBeginningWithNumberFollowedBySpaces() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123 xyz");
        assertEquals("name", "123 xyz", f.getName());
        f = getParser().parseFTPEntry("12-03-96  06:38AM       <DIR>          123 abc xyz");
        assertNotNull(f);
        assertEquals("name", "123 abc xyz", f.getName());
    }

    /**
     * Test that group names with embedded spaces can be handled correctly
     *
     */
    public void testGroupNameWithSpaces() {
        FTPFile f = getParser().parseFTPEntry("drwx------ 4 maxm Domain Users 512 Oct 2 10:59 .metadata");
        assertNotNull(f);
        assertEquals("maxm", f.getUser());
        assertEquals("Domain Users", f.getGroup());
    }

}
