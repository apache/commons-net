package org.apache.commons.net.ftp.ftp2.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 *    "Apache Turbine", nor may "Apache" appear in their name, without
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
import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@stevecoh1@attbi.com">Steve Cohen</a>
 * @version $Id: OS2FTPEntryParserTest.java,v 1.4 2002/10/09 05:11:19 brekke Exp $
 */
public class OS2FTPEntryParserTest extends FTPParseTestFramework
{

    private static final String[] badsamples = 
    {
        "                 DIR   12-30-97   12:32  jbrekke", 
        "     0    rsa    DIR   11-25-97   09:42  junk", 
        "     0           dir   05-12-97   16:44  LANGUAGE", 
        "     0           DIR   05-19-2000 12:56  local", 
        "     0           DIR   13-05-97   25:49  MPTN", 
        "587823    RSA    DIR   Jan-08-97   13:58  OS2KRNL", 
        " 33280      A          1997-02-03  13:49  OS2LDR", 
        "12-05-96  05:03PM       <DIR>          absoft2", 
        "11-14-97  04:21PM                  953 AUDITOR3.INI"
    };
    private static final String[] goodsamples = 
    {
        "     0           DIR   12-30-97   12:32  jbrekke", 
        "     0           DIR   11-25-97   09:42  junk", 
        "     0           DIR   05-12-97   16:44  LANGUAGE", 
        "     0           DIR   05-19-97   12:56  local", 
        "     0           DIR   05-12-97   16:52  Maintenance Desktop", 
        "     0           DIR   05-13-97   10:49  MPTN", 
        "587823    RSA    DIR   01-08-97   13:58  OS2KRNL", 
        " 33280      A          02-09-97   13:49  OS2LDR", 
        "     0           DIR   11-28-97   09:42  PC", 
        "149473      A          11-17-98   16:07  POPUPLOG.OS2", 
        "     0           DIR   05-12-97   16:44  PSFONTS"
    };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public OS2FTPEntryParserTest(String name)
    {
        super(name);
    }

    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {

        return (new TestSuite(OS2FTPEntryParserTest.class));
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception    
    {
        FTPFile dir = getParser().parseFTPEntry("     0           DIR   11-28-97   09:42  PC");
        assertNotNull("Could not parse entry.", dir);
        assertTrue("Should have been a directory.", 
                   dir.isDirectory());
        assertEquals(0,dir.getSize());
        assertEquals("PC", dir.getName());
        assertEquals("Fri Nov 28 09:42:00 1997", 
                     df.format(dir.getTimestamp().getTime()));
    }

    /**
     * @see org.apache.commons.net.ftp.ftp2.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception 
    {
        FTPFile file = getParser().parseFTPEntry("149473      A          11-17-98   16:07  POPUPLOG.OS2");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.", 
                   file.isFile());
        assertEquals(149473,file.getSize());
        assertEquals("POPUPLOG.OS2", file.getName());
        assertEquals("Tue Nov 17 16:07:00 1998", 
                     df.format(file.getTimestamp().getTime()));           
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

        return (new OS2FTPEntryParser());
    }
}
