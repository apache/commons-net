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
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPParseTestFramework.java,v 1.4 2004/01/02 03:39:07 scohen Exp $
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
