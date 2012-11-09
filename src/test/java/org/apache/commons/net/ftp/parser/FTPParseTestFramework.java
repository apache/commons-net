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
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
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
        for (String test : badsamples)
        {

            FTPFile f = parser.parseFTPEntry(test);
            assertNull("Should have Failed to parse " + test,
                       nullFileOrNullDate(f));

            doAdditionalBadTests(test, f);
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
        for (String test : goodsamples)
        {

            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test,
                          f);

            doAdditionalGoodTests(test, f);
        }
    }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalGoodTests(String test, FTPFile f)
    {
        }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalBadTests(String test, FTPFile f)
    {
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
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        parser = getParser();
        df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
    }

    /**
     * Check if FTPFile entry parsing failed; i.e. if entry is null or date is null.
     *
     * @param f FTPFile entry - may be null
     * @return null if f is null or the date is null
     */
    protected FTPFile nullFileOrNullDate(FTPFile f) {
        if (f==null){
            return null;
        }
        if (f.getTimestamp() == null){
            return null;
        }
        return f;
    }
}
