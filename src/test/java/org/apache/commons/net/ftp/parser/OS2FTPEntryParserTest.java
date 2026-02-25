/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.junit.jupiter.api.Test;

class OS2FTPEntryParserTest extends AbstractFTPParseTest {

    private static final String[] badsamples = { "                 DIR   12-30-97   12:32  jbrekke", "     0    rsa    DIR   11-25-97   09:42  junk",
            "     0           dir   05-12-97   16:44  LANGUAGE", "     0           DIR   13-05-97   25:49  MPTN",
            "587823    RSA    DIR   Jan-08-97   13:58  OS2KRNL", " 33280      A          1997-02-03  13:49  OS2LDR",
            "12-05-96  05:03PM       <DIR>          absoft2", "11-14-97  04:21PM                  953 AUDITOR3.INI" };

    private static final String[] goodsamples = { "     0           DIR   12-30-97   12:32  jbrekke", "     0           DIR   11-25-97   09:42  junk",
            "     0           DIR   05-12-97   16:44  LANGUAGE", "     0           DIR   05-19-97   12:56  local",
            "     0           DIR   05-12-97   16:52  Maintenance Desktop", "     0           DIR   05-13-97   10:49  MPTN",
            "587823    RSA    DIR   01-08-97   13:58  OS2KRNL", " 33280      A          02-09-97   13:49  OS2LDR",
            "     0           DIR   11-28-97   09:42  PC", "149473      A          11-17-98   16:07  POPUPLOG.OS2",
            "     0           DIR   05-12-97   16:44  PSFONTS", "     0           DIR   05-19-2000 12:56  local", };

    @Override
    protected String[] getBadListing() {

        return badsamples;
    }

    @Override
    protected String[] getGoodListing() {

        return goodsamples;
    }

    @Override
    protected FTPFileEntryParser getParser() {
        final ConfigurableFTPFileEntryParserImpl parser = new OS2FTPEntryParser();
        parser.configure(null);
        return parser;
    }

    @Override
    @Test
    void testDefaultPrecision() {
        testPrecision("     0           DIR   05-12-97   16:44  PSFONTS", CalendarUnit.MINUTE);
        testPrecision("     0           DIR   05-19-2000 12:56  local", CalendarUnit.MINUTE);
    }

    @Override
    @Test
    void testParseFieldsOnDirectory() throws Exception {
        final FTPFile dir = getParser().parseFTPEntry("     0           DIR   11-28-97   09:42  PC");
        assertNotNull(dir, "Could not parse entry.");
        assertTrue(dir.isDirectory(), "Should have been a directory.");
        assertEquals(0, dir.getSize());
        assertEquals("PC", dir.getName());
        assertEquals("Fri Nov 28 09:42:00 1997", df.format(dir.getTimestamp().getTime()));
    }

    @Override
    @Test
    void testParseFieldsOnFile() throws Exception {
        final FTPFile file = getParser().parseFTPEntry("5000000000      A          11-17-98   16:07  POPUPLOG.OS2");
        assertNotNull(file, "Could not parse entry.");
        assertTrue(file.isFile(), "Should have been a file.");
        assertEquals(5000000000L, file.getSize());
        assertEquals("POPUPLOG.OS2", file.getName());
        assertEquals("Tue Nov 17 16:07:00 1998", df.format(file.getTimestamp().getTime()));
    }

    @Override
    @Test
    void testRecentPrecision() {
        // Not needed
    }
}
