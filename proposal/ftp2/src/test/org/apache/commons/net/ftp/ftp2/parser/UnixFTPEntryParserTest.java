/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.ftp2.parser;

import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @versionn $Id$
 */
public class UnixFTPEntryParserTest extends FTPParseTestFramework
{

    private static final String[] badsamples = 
    {
        "zrwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox", 
        "dxrwr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc", 
        "drwxr-xr-x   2 root     root         4096 Jam  4 00:03 zziplib", 
        "drwxr-xr-x   2 root     99           4096 Feb 23 30:01 zzplayer", 
        "drwxr-xr-x   2 root     root         4096 Aug 36  2001 zztpp", 
        "-rw-r--r--   1 14       staff       80284 Aug 22  zxJDBC-1.2.3.tar.gz", 
        "-rw-r--r--   1 14       staff      119:26 Aug 22  2000 zxJDBC-1.2.3.zip", 
        "-rw-r--r--   1 ftp      no group    83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz", 
        "-rw-r--r--   1ftp       nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip", 
        "-rw-r--r--   1 root     root       111325 Apr -7 18:79 zxJDBC-2.0.1b1.tar.gz"
    };

    private static final String[] goodsamples = 
    {
        "-rw-r--r--   1 500      500            21 Aug  8 14:14 JB3-TES1.gz",       
        "-rwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox", 
        "drwxr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc", 
        "drwxr-xr-x   2 root     root         4096 Jan  4 00:03 zziplib", 
        "drwxr-xr-x   2 root     99           4096 Feb 23  2001 zzplayer", 
        "drwxr-xr-x   2 root     root         4096 Aug  6  2001 zztpp", 
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
        "-rw-r--r--   1 500      500       2040000 Aug  5 07:31 testRemoteUPVCopyNIX"       
    };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public UnixFTPEntryParserTest(String name)
    {
        super(name);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {
        return(badsamples);
    }
    
    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {
        return(goodsamples);
    }
    
    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser() 
    {
        return(new UnixFTPEntryParser());
    }
    
    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("drwxr-xr-x   2 user     group         4096 Mar  2 15:13 zxbox");
        assertNotNull("Could not parse entry.", 
                      f);
        assertTrue("Should have been a directory.", 
                   f.isDirectory());       
        checkPermissions(f);
        assertEquals(2, 
                     f.getHardLinkCount());
        assertEquals("user", 
                     f.getUser());
        assertEquals("group", 
                     f.getGroup());
        assertEquals("zxbox", 
                     f.getName());
        assertEquals(4096, 
                     f.getSize());
        assertEquals("Sat Mar 02 15:13:00 2002", 
                     df.format(f.getTimestamp().getTime()));
    }

    /**
     * Method checkPermissions.
     * Verify that the persmissions were properly set.
     * @param f
     */
    private void checkPermissions(FTPFile f)
    {
        assertTrue("Should have user read permission.", 
                   f.hasPermission(FTPFile.USER_ACCESS, 
                                   FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", 
                   f.hasPermission(FTPFile.USER_ACCESS, 
                                   FTPFile.WRITE_PERMISSION));
        assertTrue("Should have user execute permission.", 
                   f.hasPermission(FTPFile.USER_ACCESS, 
                                   FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", 
                   f.hasPermission(FTPFile.GROUP_ACCESS, 
                                   FTPFile.READ_PERMISSION));
        assertTrue("Should NOT have group write permission.", 
                   !f.hasPermission(FTPFile.GROUP_ACCESS, 
                                    FTPFile.WRITE_PERMISSION));
        assertTrue("Should have group execute permission.", 
                   f.hasPermission(FTPFile.GROUP_ACCESS, 
                                   FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have world read permission.", 
                   f.hasPermission(FTPFile.WORLD_ACCESS, 
                                   FTPFile.READ_PERMISSION));
        assertTrue("Should NOT have world write permission.", 
                   !f.hasPermission(FTPFile.WORLD_ACCESS, 
                                    FTPFile.WRITE_PERMISSION));
        assertTrue("Should have world execute permission.", 
                   f.hasPermission(FTPFile.WORLD_ACCESS, 
                                   FTPFile.EXECUTE_PERMISSION));
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("-rwxr-xr-x   2 user     group         4096 Mar  2 15:13 zxbox");
        assertNotNull("Could not parse entry.", 
                      f);
        assertTrue("Should have been a file.", 
                   f.isFile());
        checkPermissions(f);
        assertEquals(2, 
                     f.getHardLinkCount());
        assertEquals("user", 
                     f.getUser());
        assertEquals("group", 
                     f.getGroup());
        assertEquals("zxbox", 
                     f.getName());
        assertEquals(4096, 
                     f.getSize());
        assertEquals("Sat Mar 02 15:13:00 2002", 
                     df.format(f.getTimestamp().getTime()));
    }
    
    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(UnixFTPEntryParserTest.class));
    }
}
