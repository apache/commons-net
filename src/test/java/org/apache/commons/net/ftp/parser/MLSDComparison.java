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
 *
 */

package org.apache.commons.net.ftp.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.apache.commons.net.ftp.FTPListParseEngine;

import org.junit.Test;

/**
 * Attempt comparison of LIST and MLSD listings
 *
 * TODO - needs some work.
 */
public class MLSDComparison {

    private final Comparator<FTPFile> cmp = new Comparator<FTPFile>() {
        @Override
        public int compare(FTPFile o1, FTPFile o2) {
                String n1 = o1.getName();
                String n2 = o2.getName();
                return n1.compareTo(n2);
            }
    };

    @Test
    public void testFile() throws Exception{
        File path = new File(DownloadListings.DOWNLOAD_DIR);
        FilenameFilter filter = new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("_mlsd.txt");
            }

        };
        for (File mlsd : path.listFiles(filter)){
            System.out.println(mlsd);
            InputStream is = new FileInputStream(mlsd);
            FTPListParseEngine engine = new FTPListParseEngine(MLSxEntryParser.getInstance());
            engine.readServerList(is, FTP.DEFAULT_CONTROL_ENCODING);
            FTPFile [] mlsds = engine.getFiles(FTPFileFilters.ALL);
            is.close();
            File list = new File(mlsd.getParentFile(),mlsd.getName().replace("_mlsd", "_list"));

            System.out.println(list);
            is = new FileInputStream(list);
            FTPClientConfig cfg = new FTPClientConfig();
            cfg.setServerTimeZoneId("GMT");
            UnixFTPEntryParser parser = new UnixFTPEntryParser(cfg);
            engine = new FTPListParseEngine(parser);
            engine.readServerList(is, FTP.DEFAULT_CONTROL_ENCODING);
            FTPFile [] lists = engine.getFiles(FTPFileFilters.ALL);
            is.close();
            compareSortedLists(mlsds, lists);
        }
    }

    private void compareSortedLists(FTPFile[] lst, FTPFile[] mlst){
        Arrays.sort(lst, cmp );
        Arrays.sort(mlst, cmp );
        FTPFile first, second;
        int firstl=lst.length;
        int secondl=mlst.length;
        int one=0, two=0;
        first = lst[one++];
        second = mlst[two++];
        int cmp;
        while (one < firstl || two < secondl) {
//            String fs1 = first.toFormattedString();
//            String fs2 = second.toFormattedString();
            String rl1 = first.getRawListing();
            String rl2 = second.getRawListing();
            cmp = first.getName().compareTo(second.getName());
            if (cmp == 0) {
                if (first.getName().endsWith("HEADER.html")){
                    cmp = 0;
                }
                if (!areEquivalent(first, second)){
//                    System.out.println(rl1);
//                    System.out.println(fs1);
                    long tdiff = first.getTimestamp().getTimeInMillis()-second.getTimestamp().getTimeInMillis();
                    System.out.println("Minutes diff "+tdiff/(1000*60));
//                    System.out.println(fs2);
//                    System.out.println(rl2);
//                    System.out.println();
//                    fail();
                }
                if (one < firstl) {
                    first = lst[one++];
                }
                if (two < secondl) {
                    second = mlst[two++];
                }
            } else if (cmp < 0) {
                if (!first.getName().startsWith(".")) { // skip hidden files
                    System.out.println("1: "+rl1);
                }
                if (one < firstl) {
                    first = lst[one++];
                }
            } else {
                System.out.println("2: "+rl2);
                if (two < secondl) {
                    second = mlst[two++];
                }
            }
        }
    }
    /**
     * Compare two instances to see if they are the same,
     * ignoring any uninitialised fields.
     * @param a first instance
     * @param b second instance
     * @return true if the initialised fields are the same
     * @since 3.0
     */
    public boolean areEquivalent(FTPFile a, FTPFile b) {
        return
            a.getName().equals(b.getName()) &&
            areSame(a.getSize(), b.getSize(), -1L) &&
//            areSame(a.getUser(), b.getUser()) &&
//            areSame(a.getGroup(), b.getGroup()) &&
            areSame(a.getTimestamp(), b.getTimestamp()) &&
//            areSame(a.getType(), b.getType(), UNKNOWN_TYPE) &&
//            areSame(a.getHardLinkCount(), b.getHardLinkCount(), 0) &&
//            areSame(a._permissions, b._permissions)
            true
            ;
    }

    // compare permissions: default is all false, but that is also a possible
    // state, so this may miss some differences
//    private boolean areSame(boolean[][] a, boolean[][] b) {
//        return isDefault(a) || isDefault(b) || Arrays.deepEquals(a, b);
//    }

    // Is the array in its default state?
//    private boolean isDefault(boolean[][] a) {
//        for(boolean[] r : a){
//            for(boolean rc : r){
//                if (rc) { // not default
//                    return false;
//                }
//            }
//        }
//        return true;
//    }


    private boolean areSame(Calendar a, Calendar b) {
        return a == null || b == null || areSameDateTime(a, b);
    }

    private boolean areSameDateTime(Calendar a, Calendar b) {
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        Calendar ac = Calendar.getInstance(UTC);
        ac.setTime(a.getTime());
        Calendar bc = Calendar.getInstance(UTC);
        bc.setTime(b.getTime());
        return isSameDay(ac, bc) && isSameTime(ac, bc);
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        int ad = a.get(Calendar.DAY_OF_MONTH);
        int bd = b.get(Calendar.DAY_OF_MONTH);
        return
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
            ad == bd
            ;
    }
    private boolean isSameTime(Calendar a, Calendar b) {
        int ah = a.get(Calendar.HOUR_OF_DAY);
        int bh = b.get(Calendar.HOUR_OF_DAY);
        int am = a.get(Calendar.MINUTE);
        int bm = b.get(Calendar.MINUTE);
        int as = a.get(Calendar.SECOND);
        int bs = b.get(Calendar.SECOND);
        return
            (ah == 0 && am == 0 && as ==0) ||
            (bh == 0 && bm == 0 && bs ==0) ||
            (ah == bh && am == bm) // ignore seconds
            ;
    }


    private boolean areSame(long a, long b, long d) {
        return a == d || b == d || a == b;
    }

//    private boolean areSame(int a, int b, int d) {
//        return a == d || b == d || a == b;
//    }
//
//    private boolean areSame(String a, String b) {
//        return a.length() == 0 || b.length() == 0 || a.equals(b);
//    }
}
