package org.apache.commons.net.ftp.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;


public class DefaultFTPFileEntryParserFactoryTest extends TestCase
{
    public void testDefaultParserFactory() throws Exception {
        DefaultFTPFileEntryParserFactory factory = 
            new DefaultFTPFileEntryParserFactory();

        FTPFileEntryParser parser = factory.createFileEntryParser("unix");
        assertTrue(parser instanceof UnixFTPEntryParser);
        
        parser = factory.createFileEntryParser("UNIX");
        assertTrue(parser instanceof UnixFTPEntryParser);

        parser = factory.createFileEntryParser("Unix");
        assertTrue(parser instanceof UnixFTPEntryParser);
        
        parser = factory.createFileEntryParser("EnterpriseUnix");
        assertTrue(parser instanceof UnixFTPEntryParser);
        assertFalse(parser instanceof EnterpriseUnixFTPEntryParser);

        // works because contains the expression "Unix"
        parser = factory.createFileEntryParser("UnixFTPEntryParser");
        assertTrue(parser instanceof UnixFTPEntryParser);
        
        try {
            parser = factory.createFileEntryParser("NT");
            fail("Exception should have been thrown. \"NT\" is not a recognized key");
        } catch (ParserInitializationException pie) {
            assertNull(pie.getRootCause());
        }

        parser = factory.createFileEntryParser("WindowsNT");
        assertTrue(parser instanceof NTFTPEntryParser);
        
        parser = factory.createFileEntryParser("ThigaVMSaMaJig");
        assertTrue(parser instanceof VMSFTPEntryParser);

        parser = factory.createFileEntryParser("OS/2");
        assertTrue(parser instanceof OS2FTPEntryParser);

        try {
            parser = factory.createFileEntryParser("OS2FTPFileEntryParser");
            fail("Exception should have been thrown. \"OS2FTPFileEntryParser\" is not a recognized key");
        } catch (ParserInitializationException pie) {
            assertNull(pie.getRootCause());
        }
        
        parser = factory.createFileEntryParser(
            "org.apache.commons.net.ftp.parser.OS2FTPEntryParser");
        assertTrue(parser instanceof OS2FTPEntryParser);
        
        try {
            parser = factory.createFileEntryParser(
                "org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory");
            fail("Exception should have been thrown. \"DefaultFTPFileEntryParserFactory\" does not implement FTPFileEntryParser");
        } catch (ParserInitializationException pie) {
            Throwable root = pie.getRootCause();
            assertTrue(root instanceof ClassCastException);
        }
    }
}

