/*
 * Copyright 2001-2004 The Apache Software Foundation
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
 * Tests the EnterpriseUnixFTPEntryParser
 * 
 * @version $Id: EnterpriseUnixFTPEntryParserTest.java,v 1.6 2004/02/29 10:23:18 scolebourne Exp $
 * @author <a href="mailto:Winston.Ojeda@qg.com">Winston Ojeda</a>
 */
public class EnterpriseUnixFTPEntryParserTest extends FTPParseTestFramework
{

    private static final String[] BADSAMPLES = 
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
        "-rw-r--r--   1 root     root       111325 Apr -7 18:79 zxJDBC-2.0.1b1.tar.gz", 
        "drwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox", 
        "drwxr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc", 
        "drwxr-xr-x   2 root     root         4096 Jan  4 00:03 zziplib", 
        "drwxr-xr-x   2 root     99           4096 Feb 23  2001 zzplayer", 
        "drwxr-xr-x   2 root     root         4096 Aug  6  2001 zztpp", 
        "-rw-r--r--   1 14       staff       80284 Aug 22  2000 zxJDBC-1.2.3.tar.gz", 
        "-rw-r--r--   1 14       staff      119926 Aug 22  2000 zxJDBC-1.2.3.zip", 
        "-rw-r--r--   1 ftp      nogroup     83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz", 
        "-rw-r--r--   1 ftp      nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip", 
        "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz", 
        "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip"
    };
    private static final String[] GOODSAMPLES = 
    {
        "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST", 
        "-C--E-----FTP A QUA1I1      18128       41 Aug 12 13:56 QUADTEST2"
    };

    /**
     * Creates a new EnterpriseUnixFTPEntryParserTest object.
     * 
     * @param name Test name.
     */
    public EnterpriseUnixFTPEntryParserTest(String name)
    {
        super(name);
    }

    /**
     * Method suite.
     * 
     * @return TestSuite
     */
    public static TestSuite suite()
    {

        return (new TestSuite(EnterpriseUnixFTPEntryParserTest.class));
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {
        // Everything is a File for now.
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST");
        assertTrue("Should be a file.", 
                   file.isFile());
        assertEquals("QUADTEST", 
                     file.getName());
        assertEquals(41, 
                     file.getSize());
        assertEquals("QUA1I1", 
                     file.getUser());
        assertEquals("18128", 
                     file.getGroup());
        assertEquals("Mon Aug 12 13:56:00 2002", 
                     df.format(file.getTimestamp().getTime()));
        checkPermisions(file);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {

        return (BADSAMPLES);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {

        return (GOODSAMPLES);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {

        return (new EnterpriseUnixFTPEntryParser());
    }

    /**
     * Method checkPermisions. Verify that the parser does NOT  set the
     * permissions.
     * 
     * @param dir
     */
    private void checkPermisions(FTPFile dir)
    {
        assertTrue("Owner should not have read permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("Owner should not have write permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Owner should not have execute permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("Group should not have read permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("Group should not have write permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Group should not have execute permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("World should not have read permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("World should not have write permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("World should not have execute permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
    }
}
