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

public class MacOsPeterFTPEntryParserTest extends FTPParseTestFramework {

    private static final String[] badsamples = {
        "drwxr-xr-x    123       folder        0 Jan  4 14:49 Steak",
    };

    private static final String[] goodsamples =
    {
        "-rw-r--r--    54149       27826    81975 Jul 22  2010 09.jpg",
        "drwxr-xr-x               folder        0 Jan  4 14:51 Alias_to_Steak",
        "-rw-r--r--    78440       49231   127671 Jul 22  2010 Filename with whitespace.jpg",
        "-rw-r--r--    78440       49231   127671 Jul 22 14:51 Filename with whitespace.jpg",
        "-rw-r--r--        0      108767   108767 Jul 22  2010 presentation03.jpg",
        "-rw-r--r--    58679       60393   119072 Jul 22  2010 presentation04.jpg",
        "-rw-r--r--    82543       51433   133976 Jul 22  2010 presentation06.jpg",
        "-rw-r--r--    83616     1430976  1514592 Jul 22  2010 presentation10.jpg",
        "-rw-r--r--        0       66990    66990 Jul 22  2010 presentation11.jpg",
        "drwxr-xr-x               folder        0 Jan  4 14:49 Steak",
        "-rwx------        0       12713    12713 Jul  8  2009 Twitter_Avatar.png",
    };

    public MacOsPeterFTPEntryParserTest(String name) {
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
        return (new MacOsPeterFTPEntryParser());
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        FTPFile f = getParser().parseFTPEntry(
                "drwxr-xr-x               folder        0 Mar  2 15:13 Alias_to_Steak");
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a directory.", f.isDirectory());
        checkPermissions(f);
        assertEquals(0, f.getHardLinkCount());
        assertNull(f.getUser());
        assertNull(f.getGroup());
        assertEquals(0, f.getSize());
        assertEquals("Alias_to_Steak", f.getName());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MARCH);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (f.getTimestamp().getTime().before(cal.getTime())) {
            cal.add(Calendar.YEAR, -1);
        }
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 13);

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp()
                .getTime()));
    }

    @Override
    public void testParseFieldsOnFile() throws Exception {
        FTPFile f = getParser().parseFTPEntry(
            "-rwxr-xr-x    78440       49231   127671 Jul  2 14:51 Filename with whitespace.jpg"
            );
        assertNotNull("Could not parse entry.", f);
        assertTrue("Should have been a file.", f.isFile());
        checkPermissions(f);
        assertEquals(0, f.getHardLinkCount());
        assertNull(f.getUser());
        assertNull(f.getGroup());
        assertEquals("Filename with whitespace.jpg", f.getName());
        assertEquals(127671L, f.getSize());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JULY);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (f.getTimestamp().getTime().before(cal.getTime())) {
            cal.add(Calendar.YEAR, -1);
        }
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 51);
        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));
    }

    /**
     * Method checkPermissions.
     * Verify that the persmissions were properly set.
     * @param f
     */
    private void checkPermissions(FTPFile f) {
        assertTrue("Should have user read permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have user execute permission.", f.hasPermission(
                FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", f.hasPermission(
                FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should NOT have group write permission.", !f.hasPermission(
                FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have group execute permission.", f.hasPermission(
                FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have world read permission.", f.hasPermission(
                FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should NOT have world write permission.", !f.hasPermission(
                FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have world execute permission.", f.hasPermission(
                FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Override
    public void testDefaultPrecision() {
        testPrecision(
            "-rw-r--r--    78440       49231   127671 Jul 22  2010 Filename with whitespace.jpg", CalendarUnit.DAY_OF_MONTH);
    }

    @Override
    public void testRecentPrecision() {
        testPrecision(
            "-rw-r--r--    78440       49231   127671 Jul 22 14:51 Filename with whitespace.jpg", CalendarUnit.MINUTE);
    }

}
