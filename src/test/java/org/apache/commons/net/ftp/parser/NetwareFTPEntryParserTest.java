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

import java.util.Calendar;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 */
public class NetwareFTPEntryParserTest extends FTPParseTestFramework {

    private static final String[] badsamples = {
        "a [-----F--] SCION_SYS                         512 Apr 13 23:52 SYS",
            "d [----AF--]          0                        512 10-04-2001 _ADMIN"
    };

    private static final String [] goodsamples = {
        "d [-----F--] SCION_SYS                         512 Apr 13 23:52 SYS",
        "d [----AF--]          0                        512 Feb 22 17:32 _ADMIN",
        "d [-W---F--] SCION_VOL2                        512 Apr 13 23:12 VOL2",
        "- [RWCEAFMS] rwinston                        19968 Mar 12 15:20 Executive Summary.doc",
        "d [RWCEAFMS] rwinston                          512 Nov 24  2005 Favorites"
    };

    public NetwareFTPEntryParserTest(String name) {
        super(name);
    }

    @Override
    protected String[] getBadListing() {
        return (badsamples);
    }

    @Override
    protected String[] getGoodListing() {
        return (goodsamples);
    }

    @Override
    protected FTPFileEntryParser getParser() {
        return (new NetwareFTPEntryParser());
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        String reply = "d [-W---F--] testUser                        512 Apr 13 23:12 testFile";
        FTPFile f = getParser().parseFTPEntry(reply);

        assertNotNull("Could not parse file", f);
        assertEquals("testFile", f.getName());
        assertEquals(512L, f.getSize());
        assertEquals("testUser", f.getUser());
        assertTrue("Directory flag is not set!", f.isDirectory());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 3);
        cal.set(Calendar.DAY_OF_MONTH, 13);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 12);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.YEAR, f.getTimestamp().get(Calendar.YEAR));

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp()
                .getTime()));

    }


    @Override
    public void testParseFieldsOnFile() throws Exception {
        String reply = "- [R-CEAFMS] rwinston                        19968 Mar 12 15:20 Document name with spaces.doc";

        FTPFile f = getParser().parseFTPEntry(reply);

        assertNotNull("Could not parse file", f);
        assertEquals("Document name with spaces.doc", f.getName());
        assertEquals(19968L, f.getSize());
        assertEquals("rwinston", f.getUser());
        assertTrue("File flag is not set!", f.isFile());

        assertTrue(f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse(f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("d [RWCEAFMS] rwinston                          512 Nov 24  2005 Favorites", CalendarUnit.DAY_OF_MONTH);
    }

    @Override
    public void testRecentPrecision() {
        testPrecision("- [RWCEAFMS] rwinston                        19968 Mar 12 15:20 Executive Summary.doc", CalendarUnit.MINUTE);
    }

}


