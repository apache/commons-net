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
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPParseTestFramework.java,v 1.10 2004/02/29 10:23:18 scolebourne Exp $
 */
public abstract class FTPParseTestFramework extends TestCase
{
    private FTPFileEntryParser parser = null;
    protected SimpleDateFormat df = null;
    
    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public FTPParseTestFramework(String name)
    {
        super(name);
    }

    /**
     * Method testBadListing.
     * Tests that parser provided failures actually fail.
     * @throws Exception
     */
    public void testBadListing() throws Exception
    {

        String[] badsamples = getBadListing();
        for (int i = 0; i < badsamples.length; i++)
        {

            String test = badsamples[i];
            FTPFile f = parser.parseFTPEntry(test);
            assertNull("Should have Failed to parse " + test, 
                       f);
        }
    }

    /**
     * Method testGoodListing.
     * Test that parser provided listings pass.
     * @throws Exception
     */
    public void testGoodListing() throws Exception
    {

        String[] goodsamples = getGoodListing();
        for (int i = 0; i < goodsamples.length; i++)
        {

            String test = goodsamples[i];
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, 
                          f);
        }
    }

    /**
     * Method getBadListing.
     * Implementors must provide a listing that contains failures.
     * @return String[]
     */
    protected abstract String[] getBadListing();

    /**
     * Method getGoodListing.
     * Implementors must provide a listing that passes.
     * @return String[]
     */
    protected abstract String[] getGoodListing();

    /**
     * Method getParser.
     * Provide the parser to use for testing.
     * @return FTPFileEntryParser
     */
    protected abstract FTPFileEntryParser getParser();
    
    /**
     * Method testParseFieldsOnDirectory.
     * Provide a test to show that fields on a directory entry are parsed correctly.
     * @throws Exception
     */
    public abstract void testParseFieldsOnDirectory() throws Exception;
    
    /**
     * Method testParseFieldsOnFile.
     * Provide a test to show that fields on a file entry are parsed correctly.
     * @throws Exception
     */
    public abstract void testParseFieldsOnFile() throws Exception;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        parser = getParser();
        df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.US);
    }
}
