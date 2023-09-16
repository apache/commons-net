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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClientConfig;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Test the FTPTimestampParser class.
 */
public class FTPTimestampParserImplTest extends TestCase {

    private static final int TWO_HOURS_OF_MILLISECONDS = 2 * 60 * 60 * 1000;

    /*
     * Check how short date is interpreted at a given time. Check both with and without lenient future dates
     */
    private void checkShortParse(final String msg, final Calendar serverTime, final Calendar input) throws ParseException {
        checkShortParse(msg, serverTime, input, false);
        checkShortParse(msg, serverTime, input, true);
    }

    /**
     * Check how short date is interpreted at a given time Check only using specified lenient future dates setting
     *
     * @param msg        identifying message
     * @param servertime the time at the server
     * @param input      the time to be converted to a short date, parsed and tested against the full time
     * @param lenient    whether to use lenient mode or not.
     */
    private void checkShortParse(final String msg, final Calendar servertime, final Calendar input, final boolean lenient) throws ParseException {
        checkShortParse(msg, servertime, input, input, lenient);
    }

    /*
     * Check how short date is interpreted at a given time. Check both with and without lenient future dates
     */
    private void checkShortParse(final String msg, final Calendar serverTime, final Calendar input, final Calendar expected) throws ParseException {
        checkShortParse(msg, serverTime, input, expected, false);
        checkShortParse(msg, serverTime, input, expected, true);
    }

    /**
     * Check how short date is interpreted at a given time Check only using specified lenient future dates setting
     *
     * @param msg        identifying message
     * @param servertime the time at the server
     * @param input      the time to be converted to a short date and parsed
     * @param expected   the expected result from parsing
     * @param lenient    whether to use lenient mode or not.
     */
    private void checkShortParse(final String msg, final Calendar servertime, final Calendar input, final Calendar expected, final boolean lenient)
            throws ParseException {
        final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        parser.setLenientFutureDates(lenient);
        final SimpleDateFormat shortFormat = parser.getRecentDateFormat(); // It's expecting this format

        final String shortDate = shortFormat.format(input.getTime());
        final Calendar output = parser.parseTimestamp(shortDate, servertime);
        final int outyear = output.get(Calendar.YEAR);
        final int outdom = output.get(Calendar.DAY_OF_MONTH);
        final int outmon = output.get(Calendar.MONTH);
        final int inyear = expected.get(Calendar.YEAR);
        final int indom = expected.get(Calendar.DAY_OF_MONTH);
        final int inmon = expected.get(Calendar.MONTH);
        if (indom != outdom || inmon != outmon || inyear != outyear) {
            final Format longFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            fail("Test: '" + msg + "' Server=" + longFormat.format(servertime.getTime()) + ". Failed to parse " + shortDate
                    + (lenient ? " (lenient)" : " (non-lenient)") + " using " + shortFormat.toPattern() + ". Actual " + longFormat.format(output.getTime())
                    + ". Expected " + longFormat.format(expected.getTime()));
        }
    }

    // This test currently fails, because we assume that short dates are +-6months when parsing Feb 29
    public void DISABLEDtestNET446() throws Exception {
        final GregorianCalendar server = new GregorianCalendar(2001, Calendar.JANUARY, 1, 12, 0);
        // Note: we use a known leap year for the target date to avoid rounding up
        final GregorianCalendar input = new GregorianCalendar(2000, Calendar.FEBRUARY, 29);
        final GregorianCalendar expected = new GregorianCalendar(2000, Calendar.FEBRUARY, 29);
        checkShortParse("Feb 29th 2000", server, input, expected);
    }

    // Test leap year if current year is a leap year
    public void testFeb29IfLeapYear() throws Exception {
        final GregorianCalendar now = new GregorianCalendar();
        final int thisYear = now.get(Calendar.YEAR);
        final GregorianCalendar target = new GregorianCalendar(thisYear, Calendar.FEBRUARY, 29);
        if (now.isLeapYear(thisYear) && now.after(target) && now.before(new GregorianCalendar(thisYear, Calendar.AUGUST, 29))) {
            checkShortParse("Feb 29th", now, target);
        } else {
            System.out.println("Skipping Feb 29 test (not leap year or before Feb 29)");
        }
    }

    // Test Feb 29 for a known leap year
    public void testFeb29LeapYear() throws Exception {
        final int year = 2000; // Use same year for current and short date
        final GregorianCalendar now = new GregorianCalendar(year, Calendar.APRIL, 1, 12, 0);
        checkShortParse("Feb 29th 2000", now, new GregorianCalendar(year, Calendar.FEBRUARY, 29));
    }

    public void testFeb29LeapYear2() throws Exception {
        final int year = 2000; // Use same year for current and short date
        final GregorianCalendar now = new GregorianCalendar(year, Calendar.MARCH, 1, 12, 0);
        checkShortParse("Feb 29th 2000", now, new GregorianCalendar(year, Calendar.FEBRUARY, 29));
    }

    // same date feb 29
    public void testFeb29LeapYear3() throws Exception {
        final int year = 2000; // Use same year for current and short date
        final GregorianCalendar now = new GregorianCalendar(year, Calendar.FEBRUARY, 29, 12, 0);
        checkShortParse("Feb 29th 2000", now, new GregorianCalendar(year, Calendar.FEBRUARY, 29));
    }

    // future dated Feb 29
    public void testFeb29LeapYear4() throws Exception {
        final int year = 2000; // Use same year for current and short date
        final GregorianCalendar now = new GregorianCalendar(year, Calendar.FEBRUARY, 28, 12, 0);
        // Must allow lenient future date here
        checkShortParse("Feb 29th 2000", now, new GregorianCalendar(year, Calendar.FEBRUARY, 29), true);
    }

    // Test Feb 29 for a known non-leap year - should fail
    public void testFeb29NonLeapYear() {
        final GregorianCalendar server = new GregorianCalendar(1999, Calendar.APRIL, 1, 12, 0);
        // Note: we use a known leap year for the target date to avoid rounding up
        final GregorianCalendar input = new GregorianCalendar(2000, Calendar.FEBRUARY, 29);
        final GregorianCalendar expected = new GregorianCalendar(1999, Calendar.FEBRUARY, 29);
        assertThrows(ParseException.class, () -> checkShortParse("Feb 29th 1999", server, input, expected, true));
        assertThrows(ParseException.class, () -> checkShortParse("Feb 29th 1999", server, input, expected, false));
    }

//    Lenient mode allows for dates up to 1 day in the future

    public void testNET444() throws Exception {
        final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        parser.setLenientFutureDates(true);
        final SimpleDateFormat sdf = new SimpleDateFormat(parser.getRecentDateFormatString());
        final GregorianCalendar now = new GregorianCalendar(2012, Calendar.FEBRUARY, 28, 12, 0);

        final GregorianCalendar nowplus1 = new GregorianCalendar(2012, Calendar.FEBRUARY, 28, 13, 0);
        // Create a suitable short date
        final String future1 = sdf.format(nowplus1.getTime());
        final Calendar parsed1 = parser.parseTimestamp(future1, now);
        assertEquals(nowplus1.get(Calendar.YEAR), parsed1.get(Calendar.YEAR));

        final GregorianCalendar nowplus25 = new GregorianCalendar(2012, Calendar.FEBRUARY, 29, 13, 0);
        // Create a suitable short date
        final String future25 = sdf.format(nowplus25.getTime());
        final Calendar parsed25 = parser.parseTimestamp(future25, now);
        assertEquals(nowplus25.get(Calendar.YEAR) - 1, parsed25.get(Calendar.YEAR));
    }

    public void testParseDec31Lenient() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2007, Calendar.DECEMBER, 30, 12, 0);
        checkShortParse("2007-12-30", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_YEAR, +1); // tomorrow
        checkShortParse("2007-12-31", now, target, true);
    }

    public void testParseJan01() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2007, Calendar.JANUARY, 1, 12, 0);
        checkShortParse("2007-01-01", now, now); // should always work
        final GregorianCalendar target = new GregorianCalendar(2006, Calendar.DECEMBER, 31, 12, 0);
        checkShortParse("2006-12-31", now, target, true);
        checkShortParse("2006-12-31", now, target, false);
    }

    public void testParseJan01Lenient() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2007, Calendar.DECEMBER, 31, 12, 0);
        checkShortParse("2007-12-31", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_YEAR, +1); // tomorrow
        checkShortParse("2008-1-1", now, target, true);
    }

    public void testParser() throws ParseException {
        // This test requires an English Locale
        final Locale locale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
            parser.parseTimestamp("feb 22 2002");
            assertThrows(ParseException.class, () -> parser.parseTimestamp("f\u00e9v 22 2002"));

            final FTPClientConfig config = new FTPClientConfig();
            config.setDefaultDateFormatStr("d MMM yyyy");
            config.setRecentDateFormatStr("d MMM HH:mm");
            config.setServerLanguageCode("fr");
            parser.configure(config);
            assertThrows(ParseException.class, () -> parser.parseTimestamp("d\u00e9c 22 2002"), "incorrect.field.order");
            try {
                parser.parseTimestamp("22 d\u00e9c 2002");
            } catch (final ParseException e) {
                fail("failed.to.parse.french");
            }
            assertThrows(ParseException.class, () -> parser.parseTimestamp("22 dec 2002"), "incorrect.language");
            assertThrows(ParseException.class, () -> parser.parseTimestamp("29 f\u00e9v 2002"), "nonexistent.date");
            assertThrows(ParseException.class, () -> parser.parseTimestamp("22 ao\u00fb 30:02"), "bad.hour");
            assertThrows(ParseException.class, () -> parser.parseTimestamp("22 ao\u00fb 20:74"), "bad.minute");
            try {
                parser.parseTimestamp("28 ao\u00fb 20:02");
            } catch (final ParseException e) {
                fail("failed.to.parse.french.recent");
            }
        } finally {
            Locale.setDefault(locale);
        }
    }

    public void testParseShortFutureDates1() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2001, Calendar.MAY, 30, 12, 0);
        checkShortParse("2001-5-30", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_MONTH, 1);
        checkShortParse("2001-5-30 +1 day", now, target, true);
        try {
            checkShortParse("2001-5-30 +1 day", now, target, false);
            fail("Expected AssertionFailedError");
        } catch (final AssertionFailedError pe) {
            if (pe.getMessage().startsWith("Expected AssertionFailedError")) { // don't swallow our failure
                throw pe;
            }
        }
        target.add(Calendar.WEEK_OF_YEAR, 1);
//        checkShortParse("2001-5-30 +1 week",now,target);
//        target.add(Calendar.WEEK_OF_YEAR, 12);
//        checkShortParse("2001-5-30 +13 weeks",now,target);
//        target.add(Calendar.WEEK_OF_YEAR, 13);
//        checkShortParse("2001-5-30 +26 weeks",now,target);
    }

    public void testParseShortFutureDates2() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2004, Calendar.AUGUST, 1, 12, 0);
        checkShortParse("2004-8-1", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_MONTH, 1);
        checkShortParse("2004-8-1 +1 day", now, target, true);
        try {
            checkShortParse("2004-8-1 +1 day", now, target, false);
            fail("Expected AssertionFailedError");
        } catch (final AssertionFailedError pe) {
            if (pe.getMessage().startsWith("Expected AssertionFailedError")) { // don't swallow our failure
                throw pe;
            }
        }
//        target.add(Calendar.WEEK_OF_YEAR, 1);
//        checkShortParse("2004-8-1 +1 week",now,target);
//        target.add(Calendar.WEEK_OF_YEAR, 12);
//        checkShortParse("2004-8-1 +13 weeks",now,target);
//        target.add(Calendar.WEEK_OF_YEAR, 13);
//        checkShortParse("2004-8-1 +26 weeks",now,target);
    }

    public void testParseShortPastDates1() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2001, Calendar.MAY, 30, 12, 0);
        checkShortParse("2001-5-30", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.WEEK_OF_YEAR, -1);
        checkShortParse("2001-5-30 -1 week", now, target);
        target.add(Calendar.WEEK_OF_YEAR, -12);
        checkShortParse("2001-5-30 -13 weeks", now, target);
        target.add(Calendar.WEEK_OF_YEAR, -13);
        checkShortParse("2001-5-30 -26 weeks", now, target);
    }

    public void testParseShortPastDates2() throws Exception {
        final GregorianCalendar now = new GregorianCalendar(2004, Calendar.AUGUST, 1, 12, 0);
        checkShortParse("2004-8-1", now, now); // should always work
        final GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.WEEK_OF_YEAR, -1);
        checkShortParse("2004-8-1 -1 week", now, target);
        target.add(Calendar.WEEK_OF_YEAR, -12);
        checkShortParse("2004-8-1 -13 weeks", now, target);
        target.add(Calendar.WEEK_OF_YEAR, -13);
        checkShortParse("2004-8-1 -26 weeks", now, target);
    }

    public void testParseTimestamp() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        final Date anHourFromNow = cal.getTime();
        final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        final SimpleDateFormat sdf = new SimpleDateFormat(parser.getRecentDateFormatString());
        final String fmtTime = sdf.format(anHourFromNow);
        try {
            final Calendar parsed = parser.parseTimestamp(fmtTime);
            // since the timestamp is ahead of now (by one hour),
            // this must mean the file's date refers to a year ago.
            assertEquals("test.roll.back.year", 1, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));
        } catch (final ParseException e) {
            fail("Unable to parse");
        }
    }

    public void testParseTimestampAcrossTimeZones() {

        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.HOUR_OF_DAY, 1);
        final Date anHourFromNow = cal.getTime();

        cal.add(Calendar.HOUR_OF_DAY, 2);
        final Date threeHoursFromNow = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -2);

        final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();

        // assume we are FTPing a server in Chicago, two hours ahead of
        // L. A.
        final FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr(FTPTimestampParser.DEFAULT_SDF);
        config.setRecentDateFormatStr(FTPTimestampParser.DEFAULT_RECENT_SDF);
        // 2 hours difference
        config.setServerTimeZoneId("America/Chicago");
        config.setLenientFutureDates(false); // NET-407
        parser.configure(config);

        final SimpleDateFormat sdf = (SimpleDateFormat) parser.getRecentDateFormat().clone();

        // assume we're in the US Pacific Time Zone
        final TimeZone tzla = TimeZone.getTimeZone("America/Los_Angeles");
        sdf.setTimeZone(tzla);

        // get formatted versions of time in L.A.
        final String fmtTimePlusOneHour = sdf.format(anHourFromNow);
        final String fmtTimePlusThreeHours = sdf.format(threeHoursFromNow);

        try {
            final Calendar parsed = parser.parseTimestamp(fmtTimePlusOneHour);
            // the only difference should be the two hours
            // difference, no rolling back a year should occur.
            assertEquals("no.rollback.because.of.time.zones", TWO_HOURS_OF_MILLISECONDS, cal.getTime().getTime() - parsed.getTime().getTime());
        } catch (final ParseException e) {
            fail("Unable to parse " + fmtTimePlusOneHour);
        }

        // but if the file's timestamp is THREE hours ahead of now, that should
        // cause a rollover even taking the time zone difference into account.
        // Since that time is still later than ours, it is parsed as occurring
        // on this date last year.
        try {
            final Calendar parsed = parser.parseTimestamp(fmtTimePlusThreeHours);
            // rollback should occur here.
            assertEquals("rollback.even.with.time.zones", 1, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));
        } catch (final ParseException e) {
            fail("Unable to parse" + fmtTimePlusThreeHours);
        }
    }

    public void testParseTimestampWithSlop() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        final Calendar caltemp = (Calendar) cal.clone();
        caltemp.add(Calendar.HOUR_OF_DAY, 1);
        final Date anHourFromNow = caltemp.getTime();
        caltemp.add(Calendar.DAY_OF_MONTH, 1);
        final Date anHourFromNowTomorrow = caltemp.getTime();

        final FTPTimestampParserImpl parser = new FTPTimestampParserImpl();

        // set the "slop" factor on
        parser.setLenientFutureDates(true);

        final SimpleDateFormat sdf = new SimpleDateFormat(parser.getRecentDateFormatString());
        try {
            String fmtTime = sdf.format(anHourFromNow);
            Calendar parsed = parser.parseTimestamp(fmtTime);
            // the timestamp is ahead of now (by one hour), but
            // that's within range of the "slop" factor.
            // so the date is still considered this year.
            assertEquals("test.slop.no.roll.back.year", 0, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));

            // add a day to get beyond the range of the slop factor.
            // this must mean the file's date refers to a year ago.
            fmtTime = sdf.format(anHourFromNowTomorrow);
            parsed = parser.parseTimestamp(fmtTime);
            assertEquals("test.slop.roll.back.year", 1, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));

        } catch (final ParseException e) {
            fail("Unable to parse");
        }
    }

}
