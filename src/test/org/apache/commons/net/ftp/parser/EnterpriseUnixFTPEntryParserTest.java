package org.apache.commons.net.ftp.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Calendar;

import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * Tests the EnterpriseUnixFTPEntryParser
 * 
 * @version $Id: EnterpriseUnixFTPEntryParserTest.java,v 1.6 2004/01/02 03:39:07 scohen Exp $
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
        "drwxr-xr-x 1 usernameftp 512 Jan 29 23:32 prog",
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
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {
        // Everything is a File for now.
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST");
        Calendar today  = Calendar.getInstance();
        int year        = today.get(Calendar.YEAR);

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

        if(today.get(Calendar.MONTH) < Calendar.AUGUST)
            --year;

        Calendar timestamp = file.getTimestamp();
        assertEquals(year, timestamp.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, timestamp.get(Calendar.MONTH));
        assertEquals(12, timestamp.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, timestamp.get(Calendar.HOUR_OF_DAY));
        assertEquals(56, timestamp.get(Calendar.MINUTE));
        assertEquals(0, timestamp.get(Calendar.SECOND));

        checkPermisions(file);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {

        return (BADSAMPLES);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {

        return (GOODSAMPLES);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
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

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
