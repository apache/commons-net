/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class FTPFileTest {

    @Test
    public void testGetTimestampInstant() {
        final FTPFile file = new FTPFile();
        final Calendar timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        timestamp.set(2023, Calendar.AUGUST, 4, 23, 40, 55);
        file.setTimestamp(timestamp);

        final Instant timestampInstant = file.getTimestampInstant();

        assertNotNull(timestampInstant);
        final LocalDateTime fileDateTime = LocalDateTime.ofInstant(file.getTimestampInstant(), ZoneId.of("GMT"));
        assertAll(
                () -> assertEquals(2023, fileDateTime.getYear()),
                () -> assertEquals(Month.AUGUST, fileDateTime.getMonth()),
                () -> assertEquals(4, fileDateTime.getDayOfMonth()),
                () -> assertEquals(23, fileDateTime.getHour()),
                () -> assertEquals(40, fileDateTime.getMinute()),
                () -> assertEquals(55, fileDateTime.getSecond())
        );
    }

    @Test
    public void testGetTimestampInstantNullCalendar() {
        final FTPFile file = new FTPFile();
        assertNull(file.getTimestampInstant());
    }

    @Test
    public void testHasPermissionFalse() {
        final FTPFile file = new FTPFile();
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, false);
        assertFalse(file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION));
    }

    @Test
    public void testHasPermissionInvalidFile() {
        final FTPFile invalidFile = new FTPFile("LIST");
        assertFalse(invalidFile.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION));
    }

    @Test
    public void testHasPermissionTrue() {
        final FTPFile file = new FTPFile();
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
        assertTrue(file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
    }

    @Test
    public void testIsDirectory() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.DIRECTORY_TYPE);
        assertTrue(file.isDirectory());
    }

    @Test
    public void testIsFile() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.FILE_TYPE);
        assertTrue(file.isFile());
    }

    @Test
    public void testIsSymbolicLink() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.SYMBOLIC_LINK_TYPE);
        assertTrue(file.isSymbolicLink());
    }

    @Test
    public void testIsUnknown() {
        final FTPFile file = new FTPFile();
        assertTrue(file.isUnknown());
    }

    @Test
    public void testToString() {
        final FTPFile file = new FTPFile();
        file.setRawListing("LIST");
        assertEquals(file.getRawListing(), file.toString());
    }

    @Test
    public void testToStringDefault() {
        final FTPFile file = new FTPFile();
        assertNull(file.toString());
    }

    @Test
    public void toFormattedStringDirectoryType() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.DIRECTORY_TYPE);
        assertTrue(file.toFormattedString().startsWith("d"));
    }

    @Test
    public void toFormattedStringFileType() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.FILE_TYPE);
        assertTrue(file.toFormattedString().startsWith("-"));
    }

    @Test
    public void toFormattedStringInvalidFile() {
        final FTPFile invalidFile = new FTPFile("LIST");
        assertEquals("[Invalid: could not parse file entry]", invalidFile.toFormattedString());
    }

    @Test
    public void toFormattedStringSymbolicLinkType() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.SYMBOLIC_LINK_TYPE);
        assertTrue(file.toFormattedString().startsWith("l"));
    }

    @Test
    public void toFormattedStringUnknownType() {
        final FTPFile file = new FTPFile();
        assertTrue(file.toFormattedString().startsWith("?"));
    }

    @Test
    public void toFormattedStringWithTimezone() {
        final FTPFile file = new FTPFile();
        file.setType(FTPFile.FILE_TYPE);
        file.setSize(32767);
        file.setUser("Apache");
        file.setGroup("Apache Group");
        file.setName("virus.bat");
        final Calendar timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timestamp.set(1969, Calendar.JULY, 16, 13, 32, 0);
        file.setTimestamp(timestamp);
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true);

        final String formattedString = file.toFormattedString("GMT");

        assertAll(
                () -> assertTrue(formattedString.startsWith("-")),
                () -> assertTrue(formattedString.startsWith("rwx", 1)),
                () -> assertTrue(formattedString.contains(file.getUser())),
                () -> assertTrue(formattedString.contains(file.getGroup())),
                () -> assertTrue(formattedString.contains(String.valueOf(file.getSize()))),
                () -> assertTrue(formattedString.contains("1969-07-16 13:32:00")),
                () -> assertTrue(formattedString.contains("GMT")),
                () -> assertTrue(formattedString.contains(file.getName()))
        );
    }

}
