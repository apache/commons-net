/*
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.commons.net.ftp;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A functional test suite for checking that site listings work.  
 * @author <a href="mailto:brekke@apache.org">Jeffrey D. Brekke</a>
 * @version $Id: ListingFunctionalTest.java,v 1.5 2004/04/06 13:31:59 brekke Exp $
 */
public class ListingFunctionalTest extends TestCase
{
    static final int HOSTNAME = 0;
    static final int INVALID_PARSERKEY = 2;
    static final int INVALID_PATH = 3;
    static final int VALID_FILENAME = 4;
    static final int VALID_PARSERKEY = 1;
    static final int VALID_PATH = 5;

    public static final Test suite()
    {
        String[][] testData = 
            {
                {
                    "ftp.ibiblio.org", "unix", "vms",
                    "HA!", "javaio.jar",
                    "pub/languages/java/javafaq"
                },
                {
                    "ftp.wacom.com", "windows", "VMS", "HA!",
                    "wacom97.zip", "pub\\ftp\\drivers"
                },
                {
                    "h71000.www7.hp.com", "vms", "windows",
                    "[.HA!]", "ACLOCAL.M4;1",
					
                    "[.FREEWARE50.XTERM]"
                }
            };
        Class clasz = ListingFunctionalTest.class;
        Method[] methods = clasz.getDeclaredMethods();
        TestSuite allSuites = new TestSuite("FTP Listing Functional Test Suite");

        for (int i = 0; i < testData.length; i++)
        {
            TestSuite suite = new TestSuite(testData[i][VALID_PARSERKEY]);

            for (int j = 0; j < methods.length; j++)
            {
                Method method = methods[j];

                if (method.getName().startsWith("test"))
                {
                    suite.addTest(new ListingFunctionalTest(
                                                            method.getName(),
                                                            testData[i]));
                }
            }

            allSuites.addTest(suite);
        }

        return allSuites;
    }
    
    private FTPClient client;
    private String hostName;
    private String invalidParserKey;
    private String invalidPath;
    private String validFilename;
    private String validParserKey;
    private String validPath;

    /**
     * Constructor for FTPClientTest.
     *
     * @param arg0
     */
    public ListingFunctionalTest(String arg0,
                                 String[] settings)
    {
        super(arg0);
        invalidParserKey = settings[INVALID_PARSERKEY];
        validParserKey = settings[VALID_PARSERKEY];
        invalidPath = settings[INVALID_PATH];
        validFilename = settings[VALID_FILENAME];
        validPath = settings[VALID_PATH];
        hostName = settings[HOSTNAME];
    }

    /**
     * @param fileList
     * @param string
     *
     * @return
     */
    private boolean findByName(List fileList,
                               String string)
    {
        boolean found = false;
        Iterator iter = fileList.iterator();

        while (iter.hasNext() && !found)
        {
            Object element = iter.next();

            if (element instanceof FTPFile)
            {
                FTPFile file = (FTPFile) element;

                found = file.getName().equals(string);
            }
            else
            {
                String filename = (String) element;

                found = filename.endsWith(string);
            }
        }

        return found;
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        client = new FTPClient();
        client.connect(hostName);
        client.login("anonymous", "anonymous");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        try
        {
            client.logout();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (client.isConnected())
        {
            client.disconnect();
        }

        client = null;
        super.tearDown();
    }

    /*
     * Test for FTPListParseEngine initiateListParsing()
     */
    public void testInitiateListParsing()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        FTPListParseEngine engine = client.initiateListParsing();
        List files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    public void testInitiateListParsingWithPath()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(validParserKey,
                                                               validPath);
        List files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    public void testInitiateListParsingWithPathAndAutodetection()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(validPath);
        List files = Arrays.asList(engine.getNext(25));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String)
     */
    public void testInitiateListParsingWithPathAndAutodetectionButEmpty()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(invalidPath);

        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPListParseEngine initiateListParsing(String, String)
     */
    public void testInitiateListParsingWithPathAndIncorrectParser()
        throws IOException
    {
        FTPListParseEngine engine = client.initiateListParsing(invalidParserKey,
                                                               invalidPath);

        assertFalse(engine.hasNext());
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFiles()
        throws IOException
    {
        List files = Arrays.asList(client.listFiles(validParserKey, validPath));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    public void testListFilesWithAutodection()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        List files = Arrays.asList(client.listFiles());

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for FTPFile[] listFiles(String, String)
     */
    public void testListFilesWithIncorrectParser()
        throws IOException
    {
        FTPFile[] files = client.listFiles(invalidParserKey, validPath);

        assertEquals(0, files.length);
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    public void testListFilesWithPathAndAutodectionButEmpty()
        throws IOException
    {
        FTPFile[] files = client.listFiles(invalidPath);

        assertEquals(0, files.length);
    }

    /*
     * Test for FTPFile[] listFiles(String)
     */
    public void testListFilesWithPathAndAutodetection()
        throws IOException
    {
        List files = Arrays.asList(client.listFiles(validPath));

        assertTrue(files.toString(),
                   findByName(files, validFilename));
    }

    /*
     * Test for String[] listNames()
     */
    public void testListNames()
        throws IOException
    {
        client.changeWorkingDirectory(validPath);

        String[] names = client.listNames();

        assertNotNull(names);

        List lnames = Arrays.asList(names);

        assertTrue(lnames.toString(),
                   lnames.contains(validFilename));
    }

    /*
     * Test for String[] listNames(String)
     */
    public void testListNamesWithPath()
        throws IOException
    {
        List names = Arrays.asList(client.listNames(validPath));

        assertTrue(names.toString(),
                   findByName(names, validFilename));
    }

    public void testListNamesWithPathButEmpty()
        throws IOException
    {
        String[] names = client.listNames(invalidPath);

        assertNull(names);
    }
}
