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
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: VMSFTPEntryParserTest.java,v 1.10 2004/02/29 10:23:18 scolebourne Exp $
 */
public class VMSFTPEntryParserTest extends FTPParseTestFramework
{
    private static final String[] badsamples = 
    {

        "1-JUN.LIS;1              9/9           2-jun-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "1-JUN.LIS;2              9/9           JUN-2-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "1-JUN.LIS;2              a/9           2-JUN-98 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "DATA.DIR; 1              1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (,RWED,RWED,RE)", 
        "120196.TXT;1           118/126        14-APR-1997 12:45:27 PM  [GROUP,OWNER]    (RWED,,RWED,RE)", 
        "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP-1,OWNER]    (RWED,RWED,RWED,RE)", 
        "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED2,RWED,RWED,RE)", 
        "AA.;2                  152/153        13-FED-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)"
    };
    
    private static final String[] goodsamples = 
    {

        "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (,RWED,RWED,RE)", 
        "120196.TXT;1           118/126        14-APR-1997 12:45:27  [GROUP,OWNER]    (RWED,,RWED,RE)", 
        "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "AA.;2                  152/153        13-FEB-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)"
    };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public VMSFTPEntryParserTest(String name)
    {
        super(name);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {

        FTPFile dir = getParser().parseFTPEntry("DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a directory.", 
                   dir.isDirectory());
        assertEquals("DATA.DIR", 
                     dir.getName());
        assertEquals(512, 
                     dir.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                     df.format(dir.getTimestamp().getTime()));
        assertEquals("GROUP", 
                     dir.getGroup());
        assertEquals("OWNER", 
                     dir.getUser());
        checkPermisions(dir);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a file.", 
                   file.isFile());
        assertEquals("1-JUN.LIS", 
                     file.getName());
        assertEquals(9 * 512, 
                     file.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                     df.format(file.getTimestamp().getTime()));
        assertEquals("GROUP", 
                     file.getGroup());
        assertEquals("OWNER", 
                     file.getUser());
        checkPermisions(file);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {

        return (badsamples);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {

        return (goodsamples);
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {

        return (new VMSFTPEntryParser());
    }

    /**
     * Method checkPermisions.
     * Verify that the VMS parser does NOT  set the permissions.
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
    
    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(VMSFTPEntryParserTest.class));
    }   
}
