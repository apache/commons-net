package org.apache.commons.net.ntp;

/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Date;
import java.util.Calendar;
import junit.framework.TestCase;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * Test class that validates assertions for the basic TimeStamp operations and comparisons.
 *
 * @author Jason Mathews, MITRE Corp
 */
public class TimeStampTest extends TestCase {

    private static final String TIME1 = "c1a9ae1c.cf6ac48d";  // Tue, Dec 17 2002 14:07:24.810 UTC
    private static final String TIME2 = "c1a9ae1c.cf6ac48f";  // Tue, Dec 17 2002 14:07:24.810 UTC
    private static final String TIME3 = "c1a9ae1d.cf6ac48e";  // Tue, Dec 17 2002 14:07:25.810 UTC

    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(TimeStampTest.class);
    }

    public void testCompare() {

        TimeStamp ts1 = new TimeStamp(TIME1);	// Tue, Dec 17 2002 14:07:24.810 UTC
        TimeStamp ts2 = new TimeStamp(TIME1);
        TimeStamp ts3 = new TimeStamp(TIME2); 	// Tue, Dec 17 2002 14:07:24.810 UTC
        TimeStamp ts4 = new TimeStamp(TIME3); 	// Tue, Dec 17 2002 14:07:25.810 UTC

        // do assertion tests on TimeStamp class
        assertEquals("equals(1,2)", ts1, ts2);
        assertTrue("compareTo(1,2)", ts1.compareTo(ts2) == 0);
        assertEquals("ntpValue(1,2)", ts1.ntpValue(), ts2.ntpValue());
        assertEquals("hashCode(1,2)", ts1.hashCode(), ts2.hashCode());
        assertEquals("ts1==ts1", ts1, ts1);

	// timestamps in ts1 (TIME1) and ts3 (TIME2) are only off by the smallest
	// fraction of a second (~200 picoseconds) so the times are not equal but
	// when converted to Java dates (in milliseconds) they will be equal.
        assertTrue("ts1 != ts3", !ts1.equals(ts3));
        assertTrue("compareTo(1,3)", ts1.compareTo(ts3) == -1);
        assertEquals("seconds", ts1.getSeconds(), ts3.getSeconds());
        assertTrue("fraction", ts1.getFraction() != ts3.getFraction());
        assertTrue("ntpValue(1,3)", ts1.ntpValue() != ts3.ntpValue());
        assertTrue("hashCode(1,3)", ts1.hashCode() != ts3.hashCode());
        long time1 = ts1.getTime();
        long time3 = ts3.getTime();
        assertEquals("equals(time1,3)", time1, time3); // ntpTime1 != ntpTime3 but JavaTime(t1) == JavaTime(t3)...

        assertTrue("ts3 != ts4", !ts3.equals(ts4));
        assertTrue("time3 != ts4.time", time3 != ts4.getTime());
    }

    public void testUTCString() {
        TimeStamp ts1 = new TimeStamp(TIME1);	// Tue, Dec 17 2002 14:07:24.810 UTC
	String actual = ts1.toUTCString();
	assertEquals("Tue, Dec 17 2002 14:07:24.810 UTC", actual);
    }

    public void testDateConversion() {
	// convert current date to NtpTimeStamp then compare Java date
	// computed from NTP timestamp with original Java date.
	Calendar refCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
	Date refDate = refCal.getTime();
	TimeStamp ts = new TimeStamp(refDate);
	assertEquals("refDate.getTime()", refDate.getTime(), ts.getTime());
	Date tsDate = ts.getDate();
	assertEquals(refDate, tsDate);
    }

}
