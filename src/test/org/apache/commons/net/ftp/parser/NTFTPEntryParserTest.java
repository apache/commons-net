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
package org.apache.commons.net.ftp.parser;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import junit.framework.TestSuite;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @versionn $Id: NTFTPEntryParserTest.java,v 1.7 2004/02/29 10:26:53 scolebourne Exp $
 */
public class NTFTPEntryParserTest extends FTPParseTestFramework
{

    private static final String [] goodsamples = {
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

            };

    private static final String [] badsamples = {
                "05-26-1995  10:57AM               143712 $LDR$",
                "20-05-97  03:31PM                  681 .bash_history",
                "12-05-96  17:03         <DIR>          absoft2",
                "05-22-97  08:08                    828 AUTOEXEC.BAK",
                "     0           DIR   05-19-97   12:56  local",
                "     0           DIR   05-12-97   16:52  Maintenance Desktop",

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
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {
        return(goodsamples);
    }
    
    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {
        return(badsamples);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {
        return(new NTFTPEntryParser());
    }
    
    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(NTFTPEntryParserTest.class));
    }
    
    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
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
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile f = getParser().parseFTPEntry("05-22-97  08:08AM                  828 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", f);
        assertEquals("Thu May 22 08:08:00 1997", 
                     df.format(f.getTimestamp().getTime()));
        assertTrue("Should have been a file.", 
                   f.isFile());
        assertEquals("AUTOEXEC.BAK", f.getName());
        assertEquals(828, f.getSize());   
    }

    /* (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testGoodListing()
	 */
	public void testGoodListing() throws Exception {
		String[] goodsamples = getGoodListing();
		for (int i = 0; i < goodsamples.length; i++)
		{

			String test = goodsamples[i];
			FTPFile f = getParser().parseFTPEntry(test);
			assertNotNull("Failed to parse " + test, 
					f);
			if (test.indexOf("<DIR>") >= 0) {
				assertEquals("directory.type", 
						FTPFile.DIRECTORY_TYPE, f.getType());
			} else {
				assertEquals("file.type", 
						FTPFile.FILE_TYPE, f.getType());
			}
		}
		
	}
	

	/**
	 * test condition reported as bug 20259.
	 * directory with name beginning with a numeric character
	 * was not parsing correctly
	 * @throws Exception
	 */
	public void testDirectoryBeginningWithNumber() throws Exception {
		FTPFile f = getParser().parseFTPEntry(directoryBeginningWithNumber);
		assertEquals("name", "123xyz", f.getName());
	}
}
