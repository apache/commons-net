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
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

import junit.framework.TestCase;

/**
 */
public abstract class AbstractFTPParseTest extends TestCase {
    // associate Calendar unit ints with a readable string
    // MUST be listed least significant first, as the routine needs to
    // find the previous - less significant - entry
    protected enum CalendarUnit {
        MILLISECOND(Calendar.MILLISECOND), SECOND(Calendar.SECOND), MINUTE(Calendar.MINUTE), HOUR_OF_DAY(Calendar.HOUR_OF_DAY),
        DAY_OF_MONTH(Calendar.DAY_OF_MONTH), MONTH(Calendar.MONTH), YEAR(Calendar.YEAR);

        final int unit;

        CalendarUnit(final int calUnit) {
            unit = calUnit;
        }
    }

    private FTPFileEntryParser parser;

    protected SimpleDateFormat df;

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public AbstractFTPParseTest(final String name) {
        super(name);
    }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalBadTests(final String test, final FTPFile f) {
    }

    /**
     * during processing you could hook here to do additional tests
     *
     * @param test raw entry
     * @param f    parsed entry
     */
    protected void doAdditionalGoodTests(final String test, final FTPFile f) {
    }

    /**
     * Method getBadListing. Implementors must provide a listing that contains failures.
     *
     * @return String[]
     */
    protected abstract String[] getBadListing();

    /**
     * Method getGoodListing. Implementors must provide a listing that passes.
     *
     * @return String[]
     */
    protected abstract String[] getGoodListing();

    /**
     * Method getParser. Provide the parser to use for testing.
     *
     * @return FTPFileEntryParser
     */
    protected abstract FTPFileEntryParser getParser();

    /**
     * Check if FTPFile entry parsing failed; i.e. if entry is null or date is null.
     *
     * @param f FTPFile entry - may be null
     * @return null if f is null or the date is null
     */
    protected FTPFile nullFileOrNullDate(final FTPFile f) {
        if (f == null) {
            return null;
        }
        if (f.getTimestamp() == null) {
            return null;
        }
        return f;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = getParser();
        df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
    }

    public void testBadListing() {

        final String[] badsamples = getBadListing();
        for (final String test : badsamples) {

            final FTPFile f = parser.parseFTPEntry(test);
            assertNull("Should have Failed to parse <" + test + ">", nullFileOrNullDate(f));

            doAdditionalBadTests(test, f);
        }
    }

    // Force subclasses to test precision
    public abstract void testDefaultPrecision();

    public void testGoodListing() {

        final String[] goodsamples = getGoodListing();
        for (final String test : goodsamples) {

            final FTPFile f = parser.parseFTPEntry(test);
            assertNotNull("Failed to parse " + test, f);

            doAdditionalGoodTests(test, f);
        }
    }

    /**
     * Method testParseFieldsOnDirectory. Provide a test to show that fields on a directory entry are parsed correctly.
     *
     * @throws Exception on error
     */
    public abstract void testParseFieldsOnDirectory() throws Exception;

    /**
     * Method testParseFieldsOnFile. Provide a test to show that fields on a file entry are parsed correctly.
     *
     * @throws Exception on error
     */
    public abstract void testParseFieldsOnFile() throws Exception;

    protected void testPrecision(final String listEntry, final CalendarUnit expectedPrecision) {
        final FTPFile file = getParser().parseFTPEntry(listEntry);
        assertNotNull("Could not parse " + listEntry, file);
        final Calendar stamp = file.getTimestamp();
        assertNotNull("Failed to parse time in " + listEntry, stamp);
        final Instant instant = file.getTimestampInstant();
        assertNotNull("Failed to parse time in " + listEntry, instant);
        final int ordinal = expectedPrecision.ordinal();
        final CalendarUnit[] values = CalendarUnit.values();
        // Check expected unit and all more significant ones are set
        // This is needed for FTPFile.toFormattedString() to work correctly
        for (int i = ordinal; i < values.length; i++) {
            final CalendarUnit unit = values[i];
            assertTrue("Expected set " + unit + " in " + listEntry, stamp.isSet(unit.unit));
        }
        // Check previous entry (if any) is not set
        // This is also needed for FTPFile.toFormattedString() to work correctly
        if (ordinal > 0) {
            final CalendarUnit prevUnit = values[ordinal - 1];
            assertFalse("Expected not set " + prevUnit + " in " + listEntry, stamp.isSet(prevUnit.unit));
        }
    }

    public abstract void testRecentPrecision();
}
