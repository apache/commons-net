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
 * @version $Id: UnixFTPEntryParserTest.java 1643407 2014-12-05 19:32:00Z sebb $
 */
public class MLSxEntryParserTest extends FTPParseTestFramework {

    private static final String[] badsamples = {
        "Type=cdir;Modify=20141022065101;UNIX.mode=0775;/no/space", // no space between facts and name
        "Type=cdir;Modify=20141022065103;UNIX.mode=0775;", // no name or space
        "/no/leading/space",
        "", //empty
        "Type=cdir;Modify=20141022065102;UNIX.mode=0775; ", // no name
        "Type=dir;Size; missing =size",
        "Type=dir missing-semicolon",
        "Type= missing value and semicolon",
        " ", // no path
        "Modify=2014; Short stamp",
        "Type=pdir;Modify=20141205180002Z; /trailing chars in Modify",
        "Type=dir;Modify=2014102206510x2.999;UNIX.mode=0775; modify has spurious chars",
    };

    private static final String[] goodsamples = {
        "Type=cdir;Modify=20141022065102;UNIX.mode=0775; /commons/net",
        "Type=pdir;Modify=20141205180002;UNIX.mode=0775; /commons",
        "Type=file;Size=431;Modify=20130303210732;UNIX.mode=0664; HEADER.html",
        "Type=file;Size=1880;Modify=20130611172748;UNIX.mode=0664; README.html",
        "Type=file;Size=2364;Modify=20130611170131;UNIX.mode=0664; RELEASE-NOTES.txt",
        "Type=dir;Modify=20141022065102;UNIX.mode=0775; binaries",
        "Type=dir;Modify=20141022065102.999;UNIX.mode=0775; source",
        " /no/facts", // no facts
        "Type=; /empty/fact",
        "Size=; /empty/size",
        " Type=cdir;Modify=20141022065102;UNIX.mode=0775; /leading/space", // leading space before facts => it's a file name!
        "  ", // pathname of space
    };

    public MLSxEntryParserTest(String name) {
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
        return (MLSxEntryParser.getInstance());
    }

    /**
     * Check if FTPFile entry parsing failed; i.e. if entry is null.
     * We override parent check, as a null timestamp is not acceptable
     * for these tests.
     *
     * @param f FTPFile entry - may be null
     * @return null if f is null
     */
    @Override
    protected FTPFile nullFileOrNullDate(FTPFile f) {
        return f;
    }

    @Override
    public void testParseFieldsOnFile() throws Exception {
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision("Type=dir;Modify=20141022065102;UNIX.mode=0775; source", CalendarUnit.SECOND);

    }

    @Override
    public void testRecentPrecision() { // borrow this method to test milliseconds
        testPrecision("Type=dir;Modify=20141022065102.999;UNIX.mode=0775; source", CalendarUnit.MILLISECOND);
    }
}
