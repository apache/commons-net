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

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * @author <a href="mario@ops.co.at">MarioIvankovits</a>
 * @version $Id$
 */
public abstract class CompositeFTPParseTestFramework extends FTPParseTestFramework
{
    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public CompositeFTPParseTestFramework(String name)
    {
        super(name);
    }

    /**
     * @see FTPParseTestFramework#getGoodListing()
     */
    @Override
    protected String[] getGoodListing()
    {
        return (getGoodListings()[0]);
    }

    /**
     * Method getBadListing.
     * Implementors must provide multiple listing that contains failures and
     * must force the composite parser to switch the FtpEntryParser
     *
     * @return String[]
     */
    protected abstract String[][] getBadListings();

    /**
     * Method getGoodListing.
     * Implementors must provide multiple listing that passes and
     * must force the composite parser to switch the FtpEntryParser
     *
     * @return String[]
     */
    protected abstract String[][] getGoodListings();

    /**
     * @see FTPParseTestFramework#getBadListing()
     */
    @Override
    protected String[] getBadListing()
    {
        return (getBadListings()[0]);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testGoodListing()
     */
    public void testConsistentListing() throws Exception
    {
        String goodsamples[][] = getGoodListings();

        for (String[] goodsample : goodsamples)
        {
            FTPFileEntryParser parser = getParser();
            for (int j = 0; j < goodsample.length; j++)
            {
                String test = goodsample[j];
                FTPFile f = parser.parseFTPEntry(test);
                assertNotNull("Failed to parse " + test,
                        f);

                doAdditionalGoodTests(test, f);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testGoodListing()
     */
    @Override
    public void testBadListing() throws Exception
    {
        String badsamples[][] = getBadListings();

        for (String[] badsample : badsamples)
        {
            FTPFileEntryParser parser = getParser();
            for (int j = 0; j < badsample.length; j++)
            {
                String test = badsample[j];
                FTPFile f = parser.parseFTPEntry(test);
                assertNull("Should have Failed to parse " + test,
                        nullFileOrNullDate(f));

                doAdditionalBadTests(test, f);
            }
        }
    }

    // even though all these listings are good using one parser
    // or the other, this tests that a parser that has succeeded
    // on one format will fail if another format is substituted.
    public void testInconsistentListing() throws Exception
    {
        String goodsamples[][] = getGoodListings();

        FTPFileEntryParser parser = getParser();

        for (int i = 0; i < goodsamples.length; i++)
        {
            String test = goodsamples[i][0];
            FTPFile f = parser.parseFTPEntry(test);

            switch (i)
            {
            case 0:
                assertNotNull("Failed to parse " + test, f);
                break;
            case 1:
                assertNull("Should have failed to parse " + test, f);
                break;
            }
        }
    }
}
