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
            assertTrue(pie.getMessage()+ "should contain 'Unknown parser type:'",
                    pie.getMessage().contains("Unknown parser type:"));
        }

        parser = factory.createFileEntryParser("WindowsNT");
        assertTrue(parser instanceof CompositeFileEntryParser);

        parser = factory.createFileEntryParser("ThigaVMSaMaJig");
        assertTrue(parser instanceof VMSFTPEntryParser);

        parser = factory.createFileEntryParser("OS/2");
        assertTrue(parser instanceof OS2FTPEntryParser);

        parser = factory.createFileEntryParser("OS/400");
        assertTrue(parser instanceof CompositeFileEntryParser);

        parser = factory.createFileEntryParser("AS/400");
        assertTrue(parser instanceof CompositeFileEntryParser);

        // Added test to make sure it handles the Unix systems that were
        // compiled with OS as "UNKNOWN". This test validates that the
        // check is case-insensitive.
        parser = factory.createFileEntryParser("UNKNOWN Type: L8");

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

