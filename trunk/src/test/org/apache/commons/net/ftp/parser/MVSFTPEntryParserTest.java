/*
 * Copyright 2005 The Apache Software Foundation
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

import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * Created on Apr 6, 2005<br/>
 * @author <a href="mailto:wnoto@openfinance.com">William Noto</a>
 * @version $Id: NTFTPEntryParserTest.java,v 1.16 2005/01/02 03:17:50 scohen Exp $
 */
public class MVSFTPEntryParserTest extends FTPParseTestFramework 
{
    private static final String [] goodsamples  = 
    {
        "Migrated                                                file1.I",
        "Migrated                                                file2.I",
        "PSMLC1 3390   2005/04/04  1    1  VB   27994 27998  PS  file3.I",
        "PSMLB9 3390   2005/04/04  1    1  VB   27994 27998  PS  file4.I.BU",
        "PSMLB6 3390   2005/04/05  1    1  VB   27994 27998  PS  file3.I.BU",
        "PSMLC6 3390   2005/04/05  1    1  VB   27994 27998  PS  file6.I",
        "Migrated                                                file6.O",
        "PSMLB7 3390   2005/04/04  1    1  VB   27994 27998  PS  file7.O",
        "PSMLC6 3390   2005/04/05  1    1  VB   27994 27998  PS  file7.O.BU",
    	"FPFS42 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.RPTBM023.D061704",
    	"FPFS41 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.RPTBM056.D061704",
    	"FPFS25 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.WTM204.D061704",                
    };
    
    private static final String [] badsamples = 
    {
        "MigratedP201.$FTXPBI1.$CF2ITB.$AAB0402.I",
        "PSMLC133902005/04/041VB2799427998PSfile1.I",
        "file2.O",
    };
    
    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public MVSFTPEntryParserTest (String name) 
    {
        super(name);
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getBadListings()
     */
    protected String[] getBadListing() {
        return badsamples;
    }
    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getGoodListings()
     */
    protected String[] getGoodListing() {
        return goodsamples;
    }

    
    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
        {
            new MVSFTPEntryParser(),
        });
    }
    
    /**
     * Method suite.
     * 
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(MVSFTPEntryParserTest.class));
    }
    
    public void testParseFieldsOnDirectory() throws Exception
    {
        // I don't really know how to test this because the MVS system that I 
        // connect with does not allow me to create directories.         
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception {
        FTPFile file = getParser().parseFTPEntry("Migrated                                                file1.I");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.", file.isFile());
        assertEquals("file1.I", file.getName());
        
        FTPFile file2 = getParser().parseFTPEntry("PSMLC1 3390   2005/04/04  1    1  VB   27994 27998  PS  file2.I");
        assertNotNull("Could not parse entry.", file2);
        assertTrue("Should have been a file.", file2.isFile());
        assertEquals("file2.I", file2.getName());
    }    
}
