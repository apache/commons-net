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

import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

import junit.framework.TestSuite;

/**
 * @author <a href="mailto:scohen@stevecoh1@attbi.com">Steve Cohen</a>
 * @versionn $Id: NTFTPEntryParserTest.java,v 1.2 2002/08/06 20:32:04 brekke Exp $
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

    public NTFTPEntryParserTest (String name)
    {
        super(name);
    }

    protected String[] getGoodListing()
    {
        return(goodsamples);
    }
    
    protected String[] getBadListing()
    {
        return(badsamples);
    }

    protected FTPFileEntryParser getParser()
    {
        return(new NTFTPEntryParser());
    }
    
    public static TestSuite suite()
    {
        return(new TestSuite(NTFTPEntryParserTest.class));
    }
}
