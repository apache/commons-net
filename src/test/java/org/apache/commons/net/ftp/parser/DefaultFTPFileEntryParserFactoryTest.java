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

import org.apache.commons.net.ftp.FTPClientConfig;
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
        assertFalse(((UnixFTPEntryParser)parser).trimLeadingSpaces);

        parser = factory.createFileEntryParser("UNIX_LTRIM");
        assertTrue(parser instanceof UnixFTPEntryParser);
        assertTrue(((UnixFTPEntryParser)parser).trimLeadingSpaces);

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
            assertNull(pie.getCause());
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
            assertNull(pie.getCause());
        }

        parser = factory.createFileEntryParser(
            "org.apache.commons.net.ftp.parser.OS2FTPEntryParser");
        assertTrue(parser instanceof OS2FTPEntryParser);

        try {
            parser = factory.createFileEntryParser(
                "org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory");
            fail("Exception should have been thrown. \"DefaultFTPFileEntryParserFactory\" does not implement FTPFileEntryParser");
        } catch (ParserInitializationException pie) {
            Throwable root = pie.getCause();
            assertTrue(root instanceof ClassCastException);
        }

        try {
            // Class exists, but is an interface
            factory.createFileEntryParser("org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory");
            fail("ParserInitializationException should have been thrown.");
        } catch (ParserInitializationException pie){
            Throwable root = pie.getCause();
            assertTrue(root instanceof InstantiationException);
        }
        try {
            // Class exists, but is abstract
            factory.createFileEntryParser("org.apache.commons.net.ftp.FTPFileEntryParserImpl");
            fail("ParserInitializationException should have been thrown.");
        } catch (ParserInitializationException pie){
            Throwable root = pie.getCause();
            assertTrue(root instanceof InstantiationException);
        }
    }

    private void checkParserClass(FTPFileEntryParserFactory fact, String key, Class<?> expected){
        FTPClientConfig config = key == null ? new FTPClientConfig() : new FTPClientConfig(key);
        FTPFileEntryParser parser = fact.createFileEntryParser(config);
        assertNotNull(parser);
        assertTrue("Expected "+expected.getCanonicalName()+" got "+parser.getClass().getCanonicalName(),
                expected.isInstance(parser));
    }
    public void testDefaultParserFactoryConfig() throws Exception {
        DefaultFTPFileEntryParserFactory factory =
            new DefaultFTPFileEntryParserFactory();

        try {
            factory.createFileEntryParser((FTPClientConfig)null);
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
            // expected
        }
        checkParserClass(factory, null, UnixFTPEntryParser.class);

        checkParserClass(factory, FTPClientConfig.SYST_OS400, OS400FTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_AS400, CompositeFileEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_L8, UnixFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_MVS, MVSFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_NETWARE, NetwareFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_NT, NTFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_OS2, OS2FTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_UNIX, UnixFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_VMS, VMSFTPEntryParser.class);
        checkParserClass(factory, FTPClientConfig.SYST_MACOS_PETER, MacOsPeterFTPEntryParser.class);

        checkParserClass(factory, "WINDOWS", NTFTPEntryParser.class); // Same as SYST_NT
        // This is the way it works at present; config matching is exact
        checkParserClass(factory, "Windows", CompositeFileEntryParser.class);

        checkParserClass(factory, "OS/400", OS400FTPEntryParser.class); // Same as SYST_OS400
        // This is the way it works at present; config matching is exact
        checkParserClass(factory, "OS/400 v1", CompositeFileEntryParser.class);

        // Note: exact matching via config is the only way to generate NTFTPEntryParser and OS400FTPEntryParser
        // using DefaultFTPFileEntryParserFactory
    }
}
