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
package org.apache.commons.net.ntp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/**
 * Test class that validates assertions for the basic TimeStamp operations and comparisons.
 */
public class TimeStampTest {

    private static final String TIME1 = "c1a9ae1c.cf6ac48d"; // Tue, Dec 17 2002 14:07:24.810 UTC
    private static final String TIME2 = "c1a9ae1c.cf6ac48f"; // Tue, Dec 17 2002 14:07:24.810 UTC
    private static final String TIME3 = "c1a9ae1d.cf6ac48e"; // Tue, Dec 17 2002 14:07:25.810 UTC

    @Test
    public void testCompare() {

        final TimeStamp ts1 = new TimeStamp(TIME1); // Tue, Dec 17 2002 14:07:24.810 UTC
        final TimeStamp ts2 = new TimeStamp(TIME1);
        final TimeStamp ts3 = new TimeStamp(TIME2); // Tue, Dec 17 2002 14:07:24.810 UTC
        final TimeStamp ts4 = new TimeStamp(TIME3); // Tue, Dec 17 2002 14:07:25.810 UTC

        // do assertion tests on TimeStamp class
        assertEquals(ts1, ts2, "equals(1,2)");
        assertEquals(0, ts1.compareTo(ts2), "compareTo(1,2)");
        assertEquals(ts1.ntpValue(), ts2.ntpValue(), "ntpValue(1,2)");
        assertEquals(ts1.hashCode(), ts2.hashCode(), "hashCode(1,2)");
        assertEquals(ts1, ts1, "ts1==ts1");

        // timestamps in ts1 (TIME1) and ts3 (TIME2) are only off by the smallest
        // fraction of a second (~200 picoseconds) so the times are not equal but
        // when converted to Java dates (in milliseconds) they will be equal.
        assertFalse(ts1.equals(ts3), "ts1 != ts3");
        assertEquals(-1, ts1.compareTo(ts3), "compareTo(1,3)");
        assertEquals(ts1.getSeconds(), ts3.getSeconds(), "seconds");
        assertTrue(ts1.getFraction() != ts3.getFraction(), "fraction");
        assertTrue(ts1.ntpValue() != ts3.ntpValue(), "ntpValue(1,3)");
        assertTrue(ts1.hashCode() != ts3.hashCode(), "hashCode(1,3)");
        final long time1 = ts1.getTime();
        final long time3 = ts3.getTime();
        assertEquals(time1, time3, "equals(time1,3)"); // ntpTime1 != ntpTime3 but JavaTime(t1) == JavaTime(t3)...

        assertFalse(ts3.equals(ts4), "ts3 != ts4");
        assertTrue(time3 != ts4.getTime(), "time3 != ts4.time");
    }

    @Test
    public void testDateConversion() {
        // convert current date to NtpTimeStamp then compare Java date
        // computed from NTP timestamp with original Java date.
        final Calendar refCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        final Date refDate = refCal.getTime();
        final TimeStamp ts = new TimeStamp(refDate);
        assertEquals(refDate.getTime(), ts.getTime(), "refDate.getTime()");
        final Date tsDate = ts.getDate();
        assertEquals(refDate, tsDate);
    }

    @Test
    public void testNotSame() {
        final TimeStamp time = TimeStamp.getCurrentTime();
        Object other = Integer.valueOf(0);
        if (time.equals(other)) {
            fail("TimeStamp cannot equal Date");
        }
        other = null;
        if (time.equals(other)) {
            fail("TimeStamp cannot equal null");
        }
    }

    @Test
    public void testUTCString() {
        final TimeStamp ts1 = new TimeStamp(TIME1); // Tue, Dec 17 2002 14:07:24.810 UTC
        final String actual = ts1.toUTCString();
        assertEquals("Tue, Dec 17 2002 14:07:24.810 UTC", actual);
    }

}
