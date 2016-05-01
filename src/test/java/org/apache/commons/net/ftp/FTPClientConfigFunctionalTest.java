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
import java.net.SocketException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.TreeSet;

import junit.framework.TestCase;

/*
 * This test was contributed in a different form by W. McDonald Buck
 * of Boulder, Colorado, to help fix some bugs with the FTPClientConfig
 * in a real world setting.  It is a perfect functional test for the
 * Time Zone functionality of FTPClientConfig.
 *
 * A publicly accessible FTP server at the US National Oceanographic and
 * Atmospheric Adminstration houses a directory which contains
 * 300 files, named sn.0000 to sn.0300. Every ten minutes or so
 * the next file in sequence is rewritten with new data. Thus the directory
 * contains observations for more than 24 hours of data.  Since the server
 * has its clock set to GMT this is an excellent functional test for any
 * machine in a different time zone.
 *
 * Noteworthy is the fact that the ftp routines in some web browsers don't
 * work as well as this.  They can't, since they have no way of knowing the
 * server's time zone.  Depending on the local machine's position relative
 * to GMT and the time of day, the browsers may decide that a timestamp
 * would be in the  future if given the current year, so they assume the
 * year to be  last year.  This illustrates the value of FTPClientConfig's
 * time zone functionality.
 */

public class FTPClientConfigFunctionalTest extends TestCase {

    private final FTPClient FTP = new FTPClient();
    private FTPClientConfig FTPConf;


    /**
     *
     */
    public FTPClientConfigFunctionalTest() {
        super();

    }

    /*
     * @throws java.lang.Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FTPConf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        FTPConf.setServerTimeZoneId("GMT");
        FTP.configure(FTPConf);
        try {
            FTP.connect("tgftp.nws.noaa.gov");
            FTP.login("anonymous","testing@apache.org");
            FTP.changeWorkingDirectory("SL.us008001/DF.an/DC.sflnd/DS.metar");
            FTP.enterLocalPassiveMode();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
     * @throws java.lang.Exception
     */
    @Override
    protected void tearDown() throws Exception {
        FTP.disconnect();
        super.tearDown();
    }

    public FTPClientConfigFunctionalTest(String arg0) {
        super(arg0);
    }

    private TreeSet<FTPFile> getSortedList(FTPFile[] files) {
        // create a TreeSet which will sort each element
        // as it is added.
        TreeSet<FTPFile> sorted = new TreeSet<FTPFile>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                FTPFile f1 = (FTPFile) o1;
                FTPFile f2 = (FTPFile) o2;
                return f1.getTimestamp().getTime().compareTo(f2.getTimestamp().getTime());
            }

        });


        for (FTPFile file : files)
        {
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

    public void testTimeZoneFunctionality() throws Exception {
        java.util.Date now = new java.util.Date();
        FTPFile[] files = FTP.listFiles();
        TreeSet<FTPFile> sorted = getSortedList(files);
        //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z" );
        FTPFile lastfile = null;
        FTPFile firstfile = null;
        for (FTPFile thisfile : sorted) {
            if (firstfile == null) {
                firstfile = thisfile;
            }
            //System.out.println(sdf.format(thisfile.getTimestamp().getTime())
            //        + " " +thisfile.getName());
            if (lastfile != null) {
                // verify that the list is sorted earliest to latest.
                assertTrue(lastfile.getTimestamp()
                        .before(thisfile.getTimestamp()));
            }
            lastfile = thisfile;
        }

        if (firstfile == null || lastfile == null)  {
            fail("No files found");
        } else {
            // test that notwithstanding any time zone differences, the newest file
            // is older than now.
            assertTrue(lastfile.getTimestamp().getTime().before(now));
            Calendar first = firstfile.getTimestamp();

            // test that the oldest is less than two days older than the newest
            // and, in particular, that no files have been considered "future"
            // by the parser and therefore been relegated to the same date a
            // year ago.
            first.add(Calendar.DAY_OF_MONTH, 2);
            assertTrue(lastfile.getTimestamp().getTime().toString() +
                    " before "+ first.getTime().toString(),lastfile.getTimestamp().before(first));
        }
    }
}




