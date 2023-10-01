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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import junit.framework.TestCase;

/**
 * This is a simple TestCase that tests entry parsing using the new FTPClientConfig mechanism. The normal FTPClient cannot handle the different date formats in
 * these entries, however using a configurable format, we can handle it easily.
 *
 * The original system presenting this issue was an AIX system - see bug #27437 for details.
 */
public class FTPConfigEntryParserTest extends TestCase {

    private final SimpleDateFormat df = new SimpleDateFormat();

    /**
     * This is a new format reported on the mailing lists. Parsing this kind of entry necessitated changing the regex in the parser.
     *
     */
    public void testParseEntryWithSymlink() {

        final FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr("yyyy-MM-dd HH:mm");

        final UnixFTPEntryParser parser = new UnixFTPEntryParser();
        parser.configure(config);

        final FTPFile f = parser.parseFTPEntry("lrwxrwxrwx   1 neeme neeme    23 2005-03-02 18:06 macros");

        assertNotNull("Could not parse entry.", f);
        assertFalse("Is not a directory.", f.isDirectory());
        assertTrue("Is a symbolic link", f.isSymbolicLink());

        assertTrue("Should have user read permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have user execute permission.", f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have group write permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have group execute permission.", f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have world read permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have world write permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertTrue("Should have world execute permission.", f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        assertEquals(1, f.getHardLinkCount());

        assertEquals("neeme", f.getUser());
        assertEquals("neeme", f.getGroup());

        assertEquals("macros", f.getName());
        assertEquals(23, f.getSize());

        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 06);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.YEAR, 2005);

        assertEquals(df.format(cal.getTime()), df.format(f.getTimestamp().getTime()));

    }

    public void testParseFieldsOnAIX() {

        // Set a date format for this server type
        final FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr("dd MMM HH:mm");

        final UnixFTPEntryParser parser = new UnixFTPEntryParser();
        parser.configure(config);

        final FTPFile ftpFile = parser.parseFTPEntry("-rw-r-----   1 ravensm  sca          814 02 Mar 16:27 ZMIR2.m");

        assertNotNull("Could not parse entry.", ftpFile);
        assertFalse("Is not a directory.", ftpFile.isDirectory());

        assertTrue("Should have user read permission.", ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        assertTrue("Should have user write permission.", ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have user execute permission.", ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertTrue("Should have group read permission.", ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have group write permission.", ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have group execute permission.", ftpFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
        assertFalse("Should NOT have world read permission.", ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION));
        assertFalse("Should NOT have world write permission.", ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION));
        assertFalse("Should NOT have world execute permission.", ftpFile.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION));

        assertEquals(1, ftpFile.getHardLinkCount());

        assertEquals("ravensm", ftpFile.getUser());
        assertEquals("sca", ftpFile.getGroup());

        assertEquals("ZMIR2.m", ftpFile.getName());
        assertEquals(814, ftpFile.getSize());

        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 27);
        cal.set(Calendar.SECOND, 0);

        // With no year specified, it defaults to 1970
        // TODO this is probably a bug - it should default to the current year
        cal.set(Calendar.YEAR, 1970);

        assertEquals(df.format(cal.getTime()), df.format(ftpFile.getTimestamp().getTime()));
    }

}
