/*
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.commons.net.ftp.parser;
import java.util.Calendar;

import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * @version $Id: OS400FTPEntryParserTest.java,v 1.1 2004/03/26 12:54:57 scohen Exp $
 */

public class OS400FTPEntryParserTest extends FTPParseTestFramework
{

    private static final String[] badsamples =
    {
		"PEP              4019 04/03/18 18:58:16 STMF       einladung.zip",
		"PEP               422 03/24 14:06:26 *STMF      readme",
		"PEP              6409 04/03/24 30:06:29 *STMF      build.xml",
		"PEP USR         36864 04/03/24 14:06:34 *DIR       dir1/",
		"PEP             3686404/03/24 14:06:47 *DIR       zdir2/"
    };

    private static final String[] goodsamples =
    {
		"PEP              4019 04/03/18 18:58:16 *STMF      einladung.zip",
		"PEP               422 04/03/24 14:06:26 *STMF      readme",
		"PEP              6409 04/03/24 14:06:29 *STMF      build.xml",
		"PEP             36864 04/03/24 14:06:34 *DIR       dir1/",
		"PEP             36864 04/03/24 14:06:47 *DIR       zdir2/"
    };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public OS400FTPEntryParserTest(String name)
    {
        super(name);
    }

    /**
     * @see FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {
        return(badsamples);
    }

    /**
     * @see FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {
        return(goodsamples);
    }

    /**
     * @see FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {
        return(new OS400FTPEntryParser());
    }

    /**
     * @see FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("PEP             36864 04/03/24 14:06:34 *DIR       dir1/");
        assertNotNull("Could not parse entry.",
                      f);
        assertTrue("Should have been a directory.",
                   f.isDirectory());
        assertEquals("PEP",
                     f.getUser());
        assertEquals("dir1",
                     f.getName());
        assertEquals(36864,
                     f.getSize());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);

        cal.set(Calendar.YEAR, 2004);
		cal.set(Calendar.DATE, 24);
        cal.set(Calendar.HOUR_OF_DAY, 14);
		cal.set(Calendar.MINUTE, 6);
        cal.set(Calendar.SECOND, 34);

        assertEquals(df.format(cal.getTime()),
                     df.format(f.getTimestamp().getTime()));
    }

    /**
     * @see FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("PEP              6409 04/03/24 14:06:29 *STMF      build.xml");
        assertNotNull("Could not parse entry.",
                      f);
        assertTrue("Should have been a file.",
                   f.isFile());
        assertEquals("PEP",
                     f.getUser());
        assertEquals("build.xml",
                     f.getName());
        assertEquals(6409,
                     f.getSize());

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DATE, 24);
		cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.YEAR, 2004);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 6);
		cal.set(Calendar.SECOND, 29);
        assertEquals(df.format(cal.getTime()),
                     df.format(f.getTimestamp().getTime()));
    }

    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(OS400FTPEntryParserTest.class));
    }
}
