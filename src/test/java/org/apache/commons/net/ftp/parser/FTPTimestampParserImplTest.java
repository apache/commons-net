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
 *
 * @author scohen
 *
 */
public class FTPTimestampParserImplTest extends TestCase {

    private static final int TWO_HOURS_OF_MILLISECONDS = 2 * 60 * 60 * 1000;

    public void testParseTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date anHourFromNow = cal.getTime();
        FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        SimpleDateFormat sdf =
            new SimpleDateFormat(parser.getRecentDateFormatString());
        String fmtTime = sdf.format(anHourFromNow);
        try {
            Calendar parsed = parser.parseTimestamp(fmtTime);
            // since the timestamp is ahead of now (by one hour),
            // this must mean the file's date refers to a year ago.
            assertEquals("test.roll.back.year", 1, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));
        } catch (ParseException e) {
            fail("Unable to parse");
        }
    }

    public void testParseTimestampWithSlop() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date anHourFromNow = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date anHourFromNowTomorrow = cal.getTime();
        cal.add(Calendar.DATE, -1);

        FTPTimestampParserImpl parser = new FTPTimestampParserImpl();

        // set the "slop" factor on
        parser.setLenientFutureDates(true);

        SimpleDateFormat sdf =
            new SimpleDateFormat(parser.getRecentDateFormatString());
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

        } catch (ParseException e) {
            fail("Unable to parse");
        }
    }

    public void testParseTimestampAcrossTimeZones() {


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date anHourFromNow = cal.getTime();

        cal.add(Calendar.HOUR_OF_DAY, 2);
        Date threeHoursFromNow = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -2);

        FTPTimestampParserImpl parser = new FTPTimestampParserImpl();

        // assume we are FTPing a server in Chicago, two hours ahead of
        // L. A.
        FTPClientConfig config =
            new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        config.setDefaultDateFormatStr(FTPTimestampParser.DEFAULT_SDF);
        config.setRecentDateFormatStr(FTPTimestampParser.DEFAULT_RECENT_SDF);
        // 2 hours difference
        config.setServerTimeZoneId("America/Chicago");
        config.setLenientFutureDates(false); // NET-407
        parser.configure(config);

        SimpleDateFormat sdf = (SimpleDateFormat)
            parser.getRecentDateFormat().clone();

        // assume we're in the US Pacific Time Zone
        TimeZone tzla = TimeZone.getTimeZone("America/Los_Angeles");
        sdf.setTimeZone(tzla);

        // get formatted versions of time in L.A.
        String fmtTimePlusOneHour = sdf.format(anHourFromNow);
        String fmtTimePlusThreeHours = sdf.format(threeHoursFromNow);


        try {
            Calendar parsed = parser.parseTimestamp(fmtTimePlusOneHour);
            // the only difference should be the two hours
            // difference, no rolling back a year should occur.
            assertEquals("no.rollback.because.of.time.zones",
                TWO_HOURS_OF_MILLISECONDS,
                cal.getTime().getTime() - parsed.getTime().getTime());
        } catch (ParseException e){
            fail("Unable to parse " + fmtTimePlusOneHour);
        }

        //but if the file's timestamp is THREE hours ahead of now, that should
        //cause a rollover even taking the time zone difference into account.
        //Since that time is still later than ours, it is parsed as occurring
        //on this date last year.
        try {
            Calendar parsed = parser.parseTimestamp(fmtTimePlusThreeHours);
            // rollback should occur here.
            assertEquals("rollback.even.with.time.zones",
                    1, cal.get(Calendar.YEAR) - parsed.get(Calendar.YEAR));
        } catch (ParseException e){
            fail("Unable to parse" + fmtTimePlusThreeHours);
        }
    }


    public void testParser() {
        // This test requires an English Locale
        Locale locale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
            try {
                parser.parseTimestamp("feb 22 2002");
            } catch (ParseException e) {
                fail("failed.to.parse.default");
            }
            try {
                parser.parseTimestamp("f\u00e9v 22 2002");
                fail("should.have.failed.to.parse.default");
            } catch (ParseException e) {
                // this is the success case
            }

            FTPClientConfig config = new FTPClientConfig();
            config.setDefaultDateFormatStr("d MMM yyyy");
            config.setRecentDateFormatStr("d MMM HH:mm");
            config.setServerLanguageCode("fr");
            parser.configure(config);
            try {
                parser.parseTimestamp("d\u00e9c 22 2002");
                fail("incorrect.field.order");
            } catch (ParseException e) {
                // this is the success case
            }
            try {
                parser.parseTimestamp("22 d\u00e9c 2002");
            } catch (ParseException e) {
                fail("failed.to.parse.french");
            }

            try {
                parser.parseTimestamp("22 dec 2002");
                fail("incorrect.language");
            } catch (ParseException e) {
                // this is the success case
            }
            try {
                parser.parseTimestamp("29 f\u00e9v 2002");
                fail("nonexistent.date");
            } catch (ParseException e) {
                // this is the success case
            }

            try {
                parser.parseTimestamp("22 ao\u00fb 30:02");
                fail("bad.hour");
            } catch (ParseException e) {
                // this is the success case
            }

            try {
                parser.parseTimestamp("22 ao\u00fb 20:74");
                fail("bad.minute");
            } catch (ParseException e) {
                // this is the success case
            }
            try {
                parser.parseTimestamp("28 ao\u00fb 20:02");
            } catch (ParseException e) {
                fail("failed.to.parse.french.recent");
            }
        } finally {
            Locale.setDefault(locale);
        }
    }

    /*
     * Check how short date is interpreted at a given time.
     * Check both with and without lenient future dates
     */
    private void checkShortParse(String msg, Calendar now, Calendar input) throws ParseException {
        checkShortParse(msg, now, input, false);
        checkShortParse(msg, now, input, true);
    }

    /*
     * Check how short date is interpreted at a given time
     * Check only using specified lenient future dates setting
     */
    private void checkShortParse(String msg, Calendar now, Calendar input, boolean lenient) throws ParseException {
        FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        parser.setLenientFutureDates(lenient);
        Format shortFormat = parser.getRecentDateFormat(); // It's expecting this format
        Format longFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        final String shortDate = shortFormat.format(input.getTime());
        Calendar output=parser.parseTimestamp(shortDate, now);
        int outyear = output.get(Calendar.YEAR);
        int outdom = output.get(Calendar.DAY_OF_MONTH);
        int outmon = output.get(Calendar.MONTH);
        int inyear = input.get(Calendar.YEAR);
        int indom = input.get(Calendar.DAY_OF_MONTH);
        int inmon = input.get(Calendar.MONTH);
        if (indom != outdom || inmon != outmon || inyear != outyear){
            fail("Test: '"+msg+"' Server="+longFormat.format(now.getTime())
                    +". Failed to parse "+shortDate
                    +". Actual "+longFormat.format(output.getTime())
                    +". Expected "+longFormat.format(input.getTime()));
        }
    }

    public void testParseShortPastDates1() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2001, Calendar.MAY, 30, 12, 0);
        checkShortParse("2001-5-30",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.WEEK_OF_YEAR, -1);
        checkShortParse("2001-5-30 -1 week",now,target);
        target.add(Calendar.WEEK_OF_YEAR, -12);
        checkShortParse("2001-5-30 -13 weeks",now,target);
        target.add(Calendar.WEEK_OF_YEAR, -13);
        checkShortParse("2001-5-30 -26 weeks",now,target);
    }

    public void testParseShortPastDates2() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2004, Calendar.AUGUST, 1, 12, 0);
        checkShortParse("2004-8-1",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.WEEK_OF_YEAR, -1);
        checkShortParse("2004-8-1 -1 week",now,target);
        target.add(Calendar.WEEK_OF_YEAR, -12);
        checkShortParse("2004-8-1 -13 weeks",now,target);
        target.add(Calendar.WEEK_OF_YEAR, -13);
        checkShortParse("2004-8-1 -26 weeks",now,target);
    }

//    Lenient mode allows for dates up to 1 day in the future

    public void testParseShortFutureDates1() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2001, Calendar.MAY, 30, 12, 0);
        checkShortParse("2001-5-30",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_MONTH, 1);
        checkShortParse("2001-5-30 +1 day",now,target,true);
        try {
            checkShortParse("2001-5-30 +1 day",now,target,false);
            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError pe) {
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
        GregorianCalendar now = new GregorianCalendar(2004, Calendar.AUGUST, 1, 12, 0);
        checkShortParse("2004-8-1",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_MONTH, 1);
        checkShortParse("2004-8-1 +1 day",now,target,true);
        try {
            checkShortParse("2004-8-1 +1 day",now,target,false);
            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError pe) {
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

    // Test leap year if current year is a leap year
    public void testFeb29IfLeapYear() throws Exception{
        GregorianCalendar now = new GregorianCalendar();
        final int thisYear = now.get(Calendar.YEAR);
        if (now.isLeapYear(thisYear) && now.before(new GregorianCalendar(thisYear,Calendar.AUGUST,29))){
            GregorianCalendar target = new GregorianCalendar(thisYear,Calendar.FEBRUARY,29);
            checkShortParse("Feb 29th",now,target);
        } else {
            System.out.println("Skipping Feb 29 test");
        }
    }

    // Test Feb 29 for a known leap year
    public void testFeb29LeapYear() throws Exception{
        int year = 2000; // Use same year for current and short date
        GregorianCalendar now = new GregorianCalendar(year, Calendar.APRIL, 1, 12, 0);
        checkShortParse("Feb 29th 2000",now,new GregorianCalendar(year, Calendar.FEBRUARY,29));
    }

    // Test Feb 29 for a known non-leap year - should fail
    public void testFeb29NonLeapYear(){
        GregorianCalendar now = new GregorianCalendar(1999, Calendar.APRIL, 1, 12, 0);
        // Note: we use a known leap year for the target date to avoid rounding up
        try {
            checkShortParse("Feb 29th 1999",now,new GregorianCalendar(2000, Calendar.FEBRUARY,29));
            fail("Should have failed to parse Feb 29th 1999");
        } catch (ParseException expected) {
        }
    }

    public void testParseDec31Lenient() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2007, Calendar.DECEMBER, 30, 12, 0);
        checkShortParse("2007-12-30",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_YEAR, +1); // tomorrow
        checkShortParse("2007-12-31",now,target, true);
    }

    public void testParseJan01Lenient() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2007, Calendar.DECEMBER, 31, 12, 0);
        checkShortParse("2007-12-31",now,now); // should always work
        GregorianCalendar target = (GregorianCalendar) now.clone();
        target.add(Calendar.DAY_OF_YEAR, +1); // tomorrow
        checkShortParse("2008-1-1",now,target, true);
    }

    public void testParseJan01() throws Exception {
        GregorianCalendar now = new GregorianCalendar(2007, Calendar.JANUARY, 1, 12, 0);
        checkShortParse("2007-01-01",now,now); // should always work
        GregorianCalendar target = new GregorianCalendar(2006, Calendar.DECEMBER, 31, 12, 0);
        checkShortParse("2006-12-31",now,target, true);
        checkShortParse("2006-12-31",now,target, false);
    }

}
