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
 * @version $Id: NTFTPEntryParserTest.java,v 1.9 2004/04/06 04:40:57 scohen Exp $
 */
public class NTFTPEntryParserTest extends FTPParseTestFramework
{

    private static final String [][] goodsamples = { 
    {
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
	},
	{
			"-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz", 
			"-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip",
			"-rwxr-xr-x   2 500      500           166 Nov  2  2001 73131-testtes1.afp",
			"-rw-r--r--   1 500      500           166 Nov  9  2001 73131-testtes1.AFP",
    	}
    };
    
    private static final String [] inconsistentsamples = {
    	"-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz", 
    	"-rwxr-xr-x   2 500      500           166 Nov  2  2001 73131-testtes1.afp",
		"05-22-97  08:08AM                  828 AUTOEXEC.BAK",
		"01-22-98  01:52PM                  795 AUTOEXEC.BAT",
		"05-13-97  01:46PM                  828 AUTOEXEC.DOS",
		"12-03-96  06:38AM                  403 AUTOTOOL.LOG",
		
    };

    private static final String [] badsamples = {
                "05-26-1995  10:57AM               143712 $LDR$",
                "20-05-97  03:31PM                  681 .bash_history",
				"drwxr-xr-x   2 root     99           4096 Feb 23 30:01 zzplayer",
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
        return(goodsamples[0]);
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
		return new CompositeFileEntryParser(new FTPFileEntryParser[]
		{
			new NTFTPEntryParser(),
			new UnixFTPEntryParser()
		});
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
        FTPFile f = getParser().parseFTPEntry("05-22-97  12:08AM                  828 AUTOEXEC.BAK");
        assertNotNull("Could not parse entry.", f);
        assertEquals("Thu May 22 00:08:00 1997", 
                     df.format(f.getTimestamp().getTime()));
        assertTrue("Should have been a file.", 
                   f.isFile());
        assertEquals("AUTOEXEC.BAK", f.getName());
        assertEquals(828, f.getSize());   
    }

    /* (non-Javadoc)
	 * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testGoodListing()
	 */
	public void testConsistentListing() throws Exception {
		for (int i = 0; i < goodsamples.length; i++)
		{
			FTPFileEntryParser parser = getParser();
			for (int j = 0; j < goodsamples[i].length; j++) {
				String test = goodsamples[i][j];
				FTPFile f = parser.parseFTPEntry(test);
				assertNotNull("Failed to parse " + test, 
						f);
				if (test.indexOf("<DIR>") >= 0) {
					assertEquals("directory.type", 
							FTPFile.DIRECTORY_TYPE, f.getType());
				}
				
			}

		}
		
	}
	
	// even though all these listings are good using one parser
	// or the other, this tests that a parser that has succeeded
	// on one format will fail if another format is substituted.
	public void testInconsistentListing() throws Exception {
		FTPFileEntryParser parser = getParser();
		for (int i = 0; i < 2; i++)
		{
			String test = inconsistentsamples[i];
			FTPFile f = parser.parseFTPEntry(test);
			assertNotNull("Failed to parse " + test, f);
		}
		for (int i = 2; i < inconsistentsamples.length; i++)
		{
			String test = inconsistentsamples[i];
			FTPFile f = parser.parseFTPEntry(test);
			assertNull("Should have failed to parse " + test, f);
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
