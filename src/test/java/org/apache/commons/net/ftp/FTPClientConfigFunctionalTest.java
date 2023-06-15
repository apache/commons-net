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
package org.apache.commons.net.ftp;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.TreeSet;

import junit.framework.TestCase;

/**
 * This test was contributed in a different form by W. McDonald Buck of Boulder, Colorado, to help fix some bugs with the FTPClientConfig in a real world
 * setting. It is a perfect functional test for the Time Zone functionality of FTPClientConfig.
 *
 * A publicly accessible FTP server at the US National Oceanographic and Atmospheric Adminstration houses a directory which contains 300 files, named sn.0000 to
 * sn.0300. Every ten minutes or so the next file in sequence is rewritten with new data. Thus, the directory contains observations for more than 24 hours of
 * data. Since the server has its clock set to GMT this is an excellent functional test for any machine in a different time zone.
 *
 * Noteworthy is the fact that the FTP routines in some web browsers don't work as well as this. They can't, since they have no way of knowing the server's time
 * zone. Depending on the local machine's position relative to GMT and the time of day, the browsers may decide that a timestamp would be in the future if given
 * the current year, so they assume the year to be last year. This illustrates the value of FTPClientConfig's time zone functionality.
 */
public class FTPClientConfigFunctionalTest extends TestCase {

    private final FTPClient ftpClient = new FTPClient();
    private FTPClientConfig ftpClientConfig;

    /**
     *
     */
    public FTPClientConfigFunctionalTest() {

    }

    public FTPClientConfigFunctionalTest(final String arg0) {
        super(arg0);
    }

    private TreeSet<FTPFile> getSortedSet(final FTPFile[] files) {
        // create a TreeSet which will sort each element
        // as it is added.
        final TreeSet<FTPFile> sorted = new TreeSet<>((o1, o2) -> o1.getTimestamp().getTime().compareTo(o2.getTimestamp().getTime()));

        for (final FTPFile file : files) {
            // The directory contains a few additional files at the beginning
            // which aren't in the series we want. The series we want consists
            // of files named sn.dddd. This adjusts the file list to get rid
            // of the uninteresting ones.
            if (file.getName().startsWith("sn")) {
                sorted.add(file);
            }
        }
        return sorted;
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClientConfig.setServerTimeZoneId("GMT");
        ftpClient.configure(ftpClientConfig);
        try {
            ftpClient.connect("tgftp.nws.noaa.gov");
            ftpClient.login("anonymous", "testing@apache.org");
            ftpClient.changeWorkingDirectory("SL.us008001/DF.an/DC.sflnd/DS.metar");
            ftpClient.enterLocalPassiveMode();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception
     */
    @Override
    protected void tearDown() throws Exception {
        ftpClient.disconnect();
        super.tearDown();
    }

    public void testTimeZoneFunctionality() throws Exception {
        final java.util.Date nowDate = new java.util.Date();
        final Instant nowInstant = nowDate.toInstant();
        final FTPFile[] files = ftpClient.listFiles();
        final TreeSet<FTPFile> sortedSet = getSortedSet(files);
        // SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z" );
        FTPFile lastFile = null;
        FTPFile firstFile = null;
        for (final FTPFile thisFile : sortedSet) {
            if (firstFile == null) {
                firstFile = thisFile;
            }
            // System.out.println(sdf.format(thisFile.getTimestamp().getTime())
            // + " " +thisFile.getName());
            if (lastFile != null) {
                // verify that the list is sorted earliest to latest.
                assertTrue(lastFile.getTimestamp().before(thisFile.getTimestamp()));
                assertTrue(lastFile.getTimestampInstant().isBefore(thisFile.getTimestampInstant()));
            }
            lastFile = thisFile;
        }

        if (firstFile == null || lastFile == null) {
            fail("No files found");
        } else {
            // test that notwithstanding any time zone differences, the newest file
            // is older than now.
            assertTrue(lastFile.getTimestamp().getTime().before(nowDate));
            assertTrue(lastFile.getTimestampInstant().isBefore(nowInstant));
            final Calendar firstCal = firstFile.getTimestamp();
            final Instant firstInstant = firstFile.getTimestampInstant().plus(Duration.ofDays(2));

            // test that the oldest is less than two days older than the newest
            // and, in particular, that no files have been considered "future"
            // by the parser and therefore been relegated to the same date a
            // year ago.
            firstCal.add(Calendar.DAY_OF_MONTH, 2);
            assertTrue(lastFile.getTimestamp().getTime() + " before " + firstCal.getTime(), lastFile.getTimestamp().before(firstCal));
            assertTrue(lastFile.getTimestampInstant() + " before " + firstInstant, lastFile.getTimestampInstant().isBefore(firstInstant));
        }
    }
}
