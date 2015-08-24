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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * Test suite addapted to new MVSFTPEntryParser.java.
 */
public class MVSFTPEntryParserTest extends FTPParseTestFramework {

    private static final String[] goodsamplesDatasetList = {
            /* Note, if the string begins with SAVE, the parsed entry is stored in the List saveftpfiles */
            //  "Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname",
            "SAVE00 3390   2004/06/23  1    1  FB     128  6144  PS    INCOMING.RPTBM023.D061704",
            "SAVE01 3390   2004/06/23  1    1  FB     128  6144  PO    INCOMING.RPTBM024.D061704",
            "SAVE02 3390   2004/06/23  1    1  FB     128  6144  PO-E  INCOMING.RPTBM025.D061704",
            "PSMLC1 3390   2005/04/04  1    1  VB   27994 27998  PS    file3.I",
            "PSMLB9 3390   2005/04/04  1    1  VB   27994 27998  PS    file4.I.BU",
            "PSMLB6 3390   2005/04/05  1    1  VB   27994 27998  PS    file3.I.BU",
            "PSMLC6 3390   2005/04/05  1    1  VB   27994 27998  PS    file6.I",
            "PSMLB7 3390   2005/04/04  1    1  VB   27994 27998  PS    file7.O",
            "PSMLC6 3390   2005/04/05  1    1  VB   27994 27998  PS    file7.O.BU",
            "FPFS49 3390   2004/06/23  1    1  FB     128  6144  PO-E  INCOMING.RPTBM026.D061704",
            "FPFS41 3390   2004/06/23  1    1  FB     128  6144  PS    INCOMING.RPTBM056.D061704",
            "FPFS25 3390   2004/06/23  1    1  FB     128  6144  PS    INCOMING.WTM204.D061704", };

    private static final String[] goodsamplesMemberList = {
            /* Note, if the string begins with SAVE, the parsed entry is stored in the List saveftpfiles */
            "Name      VV.MM   Created       Changed      Size  Init   Mod   Id",
            "SAVE03    01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001",
            "SAVE04                                                              ", // no statistics
            "TBSHELF1  01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001",
            "TBSHELF2  01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001",
            "TBSHELF3  01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001",
            "TBSHELF4  01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001", };

    private static final String[] goodsamplesJES1List = { /* no header for JES1 (JES Interface level 1) */
    /* Note, if the string begins with SAVE, the parsed entry is stored in the List saveftpfiles */
    "IBMUSER1  JOB01906  OUTPUT    3 Spool Files", };

    private static final String[] goodsamplesJES2List = { /* JES2 (JES Interface level 2) */
            /* Note, if the string begins with SAVE, the parsed entry is stored in the List saveftpfiles */
            //"JOBNAME  JOBID    OWNER    STATUS CLASS",
            "IBMUSER2 JOB01906 IBMUSER  OUTPUT A        RC=0000 3 spool files",
            "IBMUSER  TSU01830 IBMUSER  OUTPUT TSU      ABEND=522 3 spool files", };

    private static final String[] badsamples = {
            "MigratedP201.$FTXPBI1.$CF2ITB.$AAB0402.I",
            "PSMLC133902005/04/041VB2799427998PSfile1.I", "file2.O", };

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public MVSFTPEntryParserTest(String name) {
        super(name);

    }

    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getBadListings()
     */
    @Override
    protected String[] getBadListing() {
        return badsamples;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.CompositeFTPParseTestFramework#getGoodListings()
     */
    @Override
    protected String[] getGoodListing() {
        return goodsamplesDatasetList;
    }

    protected List<String[]> getAllGoodListings() {
        List<String[]> l = new ArrayList<String[]>();
        l.add(goodsamplesDatasetList);
        l.add(goodsamplesMemberList);
        l.add(goodsamplesJES1List);
        l.add(goodsamplesJES2List);

        return l;
    }


    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
     */
    @Override
    protected FTPFileEntryParser getParser() {
        return new MVSFTPEntryParser();
    }

    /*
     * note the testGoodListing has to be the first test invoked, because
     * some FTPFile entries are saved for the later tests
     *
     * (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testGoodListing()
     */
    @Override
    public void testGoodListing() throws Exception {
        String[] goodsamples = getGoodListing();
        MVSFTPEntryParser parser = new MVSFTPEntryParser();
        parser.setType(MVSFTPEntryParser.FILE_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.FILE_LIST_REGEX);
        for (String test : goodsamples) {
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);
            doAdditionalGoodTests(test, f);
        }
    }

    public void testMemberListing() throws Exception {
        MVSFTPEntryParser parser = new MVSFTPEntryParser();
        parser.setType(MVSFTPEntryParser.MEMBER_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.MEMBER_LIST_REGEX);
        for (String test : goodsamplesMemberList) {
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);
            doAdditionalGoodTests(test, f);
        }
    }

    public void testJesLevel1Listing() {
        MVSFTPEntryParser parser = new MVSFTPEntryParser();
        parser.setType(MVSFTPEntryParser.JES_LEVEL_1_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.JES_LEVEL_1_LIST_REGEX);
        for (String test : goodsamplesJES1List) {
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);
            doAdditionalGoodTests(test, f);
        }
    }

    public void testJesLevel2Listing() {
        MVSFTPEntryParser parser = new MVSFTPEntryParser();
        parser.setType(MVSFTPEntryParser.JES_LEVEL_2_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.JES_LEVEL_2_LIST_REGEX);
        for (String test : goodsamplesJES2List) {
            FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);
            doAdditionalGoodTests(test, f);
        }
    }

    @Override
    public void testParseFieldsOnDirectory() throws Exception {
        MVSFTPEntryParser parser = new MVSFTPEntryParser();
        parser.setType(MVSFTPEntryParser.FILE_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.FILE_LIST_REGEX);

        FTPFile file = parser
                .parseFTPEntry("SAVE01 3390   2004/06/23  1    1  FB     128  6144  PO    INCOMING.RPTBM024.D061704");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a directory.", file.isDirectory());
        assertEquals("INCOMING.RPTBM024.D061704", file.getName());

        file = parser
                .parseFTPEntry("SAVE02 3390   2004/06/23  1    1  FB     128  6144  PO-E  INCOMING.RPTBM025.D061704");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a directory.", file.isDirectory());
        assertEquals("INCOMING.RPTBM025.D061704", file.getName());

    }

    /* (non-Javadoc)
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    @Override
    public void testParseFieldsOnFile() throws Exception {
        FTPFile file = null;

        MVSFTPEntryParser parser = new MVSFTPEntryParser();

        parser.setRegex(MVSFTPEntryParser.FILE_LIST_REGEX);
        parser.setType(MVSFTPEntryParser.FILE_LIST_TYPE);

        file = parser.parseFTPEntry("SAVE00 3390   2004/06/23  1    1  FB     128  6144  PS    INCOMING.RPTBM023.D061704");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.", file.isFile());
        assertEquals("INCOMING.RPTBM023.D061704", file.getName());
        assertNull("Timestamp should not have been set.", file.getTimestamp());

        parser.setType(MVSFTPEntryParser.MEMBER_LIST_TYPE);
        parser.setRegex(MVSFTPEntryParser.MEMBER_LIST_REGEX);

        file = parser.parseFTPEntry("SAVE03    01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.", file.isFile());
        assertEquals("SAVE03", file.getName());
        assertNotNull("Timestamp should have been set.", file.getTimestamp());

        file = parser.parseFTPEntry("SAVE04                                                              ");
        assertNotNull("Could not parse entry.", file);
        assertTrue("Should have been a file.", file.isFile());
        assertEquals("SAVE04", file.getName());
        assertNull("Timestamp should not have been set.", file.getTimestamp());

    }

    @Override
    public void testDefaultPrecision() {
        // TODO Not sure what dates are parsed
    }

    @Override
    public void testRecentPrecision() {
        // TODO Auto-generated method stub
    }
}
