/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPClientConfig;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author scohen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FTPTimestampParserImplTest extends TestCase {
	
	private static final int TWO_HOURS_OF_MILLISECONDS = 2 * 60 * 60 * 1000;

	public void testParseTimestamp() {
		Calendar cal = Calendar.getInstance();
		int timeZoneOffset = cal.getTimeZone().getRawOffset();
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
				(long)TWO_HOURS_OF_MILLISECONDS, 
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
	}
	
    /**
     * Method suite.
     *
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(FTPTimestampParserImplTest.class));
    }



}
