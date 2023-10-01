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

package org.apache.commons.net.ntp;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TimeStamp class represents the Network Time Protocol (NTP) timestamp as defined in RFC-1305 and SNTP (RFC-2030). It is represented as a 64-bit unsigned
 * fixed-point number in seconds relative to 0-hour on 1-January-1900. The 32-bit low-order bits are the fractional seconds whose precision is about 200
 * picoseconds. Assumes overflow date when date passes MAX_LONG and reverts back to 0 is 2036 and not 1900. Test for most significant bit: if MSB=0 then 2036
 * basis is used otherwise 1900 if MSB=1.
 * <p>
 * Methods exist to convert NTP timestamps to and from the equivalent Java date representation, which is the number of milliseconds since the standard base time
 * known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
 * </p>
 *
 * @see java.util.Date
 */
public class TimeStamp implements Serializable, Comparable<TimeStamp> {
    private static final long serialVersionUID = 8139806907588338737L;

    /**
     * Baseline NTP time if bit-0=0 is 7-Feb-2036 @ 06:28:16 UTC
     */
    protected static final long msb0baseTime = 2085978496000L;

    /**
     * Baseline NTP time if bit-0=1 is 1-Jan-1900 @ 01:00:00 UTC
     */
    protected static final long msb1baseTime = -2208988800000L;

    /**
     * Default NTP date string format. E.g. Fri, Sep 12 2003 21:06:23.860. See <code>java.text.SimpleDateFormat</code> for code descriptions.
     */
    public static final String NTP_DATE_FORMAT = "EEE, MMM dd yyyy HH:mm:ss.SSS";

    /**
     * Left-pad 8-character hex string with 0's
     *
     * @param buf - StringBuilder which is appended with leading 0's.
     * @param l   - a long.
     */
    private static void appendHexString(final StringBuilder buf, final long l) {
        final String s = Long.toHexString(l);
        for (int i = s.length(); i < 8; i++) {
            buf.append('0');
        }
        buf.append(s);
    }

    /**
     * Convert NTP timestamp hexstring (e.g. "c1a089bd.fc904f6d") to the NTP 64-bit unsigned fixed-point number.
     *
     * @param hexString the string to convert
     *
     * @return NTP 64-bit timestamp value.
     * @throws NumberFormatException - if the string does not contain a parsable timestamp.
     */
    protected static long decodeNtpHexString(final String hexString) throws NumberFormatException {
        if (hexString == null) {
            throw new NumberFormatException("null");
        }
        final int ind = hexString.indexOf('.');
        if (ind == -1) {
            if (hexString.isEmpty()) {
                return 0;
            }
            return Long.parseLong(hexString, 16) << 32; // no decimal
        }

        return Long.parseLong(hexString.substring(0, ind), 16) << 32 | Long.parseLong(hexString.substring(ind + 1), 16);
    }

    /**
     * Constructs a NTP timestamp object and initializes it so that it represents the time at which it was allocated, measured to the nearest millisecond.
     *
     * @return NTP timestamp object set to the current time.
     * @see System#currentTimeMillis()
     */
    public static TimeStamp getCurrentTime() {
        return getNtpTime(System.currentTimeMillis());
    }

    // initialization of static time bases
    /*
     * static { TimeZone utcZone = TimeZone.getTimeZone("UTC"); Calendar calendar = Calendar.getInstance(utcZone); calendar.set(1900, Calendar.JANUARY, 1, 0, 0,
     * 0); calendar.set(Calendar.MILLISECOND, 0); msb1baseTime = calendar.getTime().getTime(); calendar.set(2036, Calendar.FEBRUARY, 7, 6, 28, 16);
     * calendar.set(Calendar.MILLISECOND, 0); msb0baseTime = calendar.getTime().getTime(); }
     */

    /**
     * Helper method to convert Java time to NTP timestamp object. Note that Java time (milliseconds) by definition has less precision than NTP time
     * (picoseconds) so converting Ntptime to Javatime and back to Ntptime loses precision. For example, Tue, Dec 17 2002 09:07:24.810 is represented by a
     * single Java-based time value of f22cd1fc8a, but its NTP equivalent are all values from c1a9ae1c.cf5c28f5 to c1a9ae1c.cf9db22c.
     *
     * @param dateMillis the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return NTP timestamp object at the specified date.
     */
    public static TimeStamp getNtpTime(final long dateMillis) {
        return new TimeStamp(toNtpTime(dateMillis));
    }

    /**
     * Converts 64-bit NTP timestamp to Java standard time.
     *
     * Note that java time (milliseconds) by definition has less precision than NTP time (picoseconds) so converting NTP timestamp to java time and back to NTP
     * timestamp loses precision. For example, Tue, Dec 17 2002 09:07:24.810 EST is represented by a single Java-based time value of f22cd1fc8a, but its NTP
     * equivalent are all values ranging from c1a9ae1c.cf5c28f5 to c1a9ae1c.cf9db22c.
     *
     * @param ntpTimeValue the input time
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this NTP timestamp value.
     */
    public static long getTime(final long ntpTimeValue) {
        final long seconds = ntpTimeValue >>> 32 & 0xffffffffL; // high-order 32-bits
        long fraction = ntpTimeValue & 0xffffffffL; // low-order 32-bits

        // Use round-off on fractional part to preserve going to lower precision
        fraction = Math.round(1000D * fraction / 0x100000000L);

        /*
         * If the most significant bit (MSB) on the seconds field is set we use a different time base. The following text is a quote from RFC-2030 (SNTP v4):
         *
         * If bit 0 is set, the UTC time is in the range 1968-2036 and UTC time is reckoned from 0h 0m 0s UTC on 1 January 1900. If bit 0 is not set, the time
         * is in the range 2036-2104 and UTC time is reckoned from 6h 28m 16s UTC on 7 February 2036.
         */
        final long msb = seconds & 0x80000000L;
        if (msb == 0) {
            // use base: 7-Feb-2036 @ 06:28:16 UTC
            return msb0baseTime + seconds * 1000 + fraction;
        }
        // use base: 1-Jan-1900 @ 01:00:00 UTC
        return msb1baseTime + seconds * 1000 + fraction;
    }

    /**
     * Parses the string argument as a NTP hexidecimal timestamp representation string (e.g. "c1a089bd.fc904f6d").
     *
     * @param s - hexstring.
     * @return the Timestamp represented by the argument in hexidecimal.
     * @throws NumberFormatException - if the string does not contain a parsable timestamp.
     */
    public static TimeStamp parseNtpString(final String s) throws NumberFormatException {
        return new TimeStamp(decodeNtpHexString(s));
    }

    /**
     * Converts Java time to 64-bit NTP time representation.
     *
     * @param millis Java time
     * @return NTP timestamp representation of Java time value.
     */
    protected static long toNtpTime(final long millis) {
        final boolean useBase1 = millis < msb0baseTime; // time < Feb-2036
        final long baseTimeMillis;
        if (useBase1) {
            baseTimeMillis = millis - msb1baseTime; // dates <= Feb-2036
        } else {
            // if base0 needed for dates >= Feb-2036
            baseTimeMillis = millis - msb0baseTime;
        }

        long seconds = baseTimeMillis / 1000;
        final long fraction = baseTimeMillis % 1000 * 0x100000000L / 1000;

        if (useBase1) {
            seconds |= 0x80000000L; // set high-order bit if msb1baseTime 1900 used
        }

        return seconds << 32 | fraction;
    }

    /**
     * Converts 64-bit NTP timestamp value to a <code>String</code>. The NTP timestamp value is represented as hex string with seconds separated by fractional
     * seconds by a decimal point; e.g. c1a089bd.fc904f6d == Tue, Dec 10 2002 10:41:49.986
     *
     * @param ntpTime the 64 bit timestamp
     *
     * @return NTP timestamp 64-bit long value as hex string with seconds separated by fractional seconds.
     */
    public static String toString(final long ntpTime) {
        final StringBuilder buf = new StringBuilder();
        // high-order second bits (32..63) as hexstring
        appendHexString(buf, ntpTime >>> 32 & 0xffffffffL);

        // low-order fractional seconds bits (0..31) as hexstring
        buf.append('.');
        appendHexString(buf, ntpTime & 0xffffffffL);

        return buf.toString();
    }

    /**
     * NTP timestamp value: 64-bit unsigned fixed-point number as defined in RFC-1305 with high-order 32 bits the seconds field and the low-order 32-bits the
     * fractional field.
     */
    private final long ntpTime;

    private DateFormat simpleFormatter;

    private DateFormat utcFormatter;

    /**
     * Constructs a newly allocated NTP timestamp object that represents the Java Date argument.
     *
     * @param d - the Date to be represented by the Timestamp object.
     */
    public TimeStamp(final Date d) {
        ntpTime = d == null ? 0 : toNtpTime(d.getTime());
    }

    /**
     * Constructs a newly allocated NTP timestamp object that represents the native 64-bit long argument.
     *
     * @param ntpTime the timestamp
     */
    public TimeStamp(final long ntpTime) {
        this.ntpTime = ntpTime;
    }

    /**
     * Constructs a newly allocated NTP timestamp object that represents the value represented by the string in hexdecimal form (e.g. "c1a089bd.fc904f6d").
     *
     * @param hexStamp the hex timestamp
     *
     * @throws NumberFormatException - if the string does not contain a parsable timestamp.
     */
    public TimeStamp(final String hexStamp) throws NumberFormatException {
        ntpTime = decodeNtpHexString(hexStamp);
    }

    /**
     * Compares two Timestamps numerically.
     *
     * @param anotherTimeStamp - the <code>TimeStamp</code> to be compared.
     * @return the value <code>0</code> if the argument TimeStamp is equal to this TimeStamp; a value less than <code>0</code> if this TimeStamp is numerically
     *         less than the TimeStamp argument; and a value greater than <code>0</code> if this TimeStamp is numerically greater than the TimeStamp argument
     *         (signed comparison).
     */
    @Override
    public int compareTo(final TimeStamp anotherTimeStamp) {
        final long thisVal = this.ntpTime;
        final long anotherVal = anotherTimeStamp.ntpTime;
        return Long.compare(thisVal, anotherVal);
    }

    /**
     * Compares this object against the specified object. The result is {@code true} if and only if the argument is not <code>null</code> and is a
     * <code>Long</code> object that contains the same <code>long</code> value as this object.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TimeStamp) {
            return ntpTime == ((TimeStamp) obj).ntpValue();
        }
        return false;
    }

    /**
     * Converts NTP timestamp to Java Date object.
     *
     * @return NTP Timestamp in Java Date
     */
    public Date getDate() {
        return new Date(getTime(ntpTime));
    }

    /**
     * Returns low-order 32-bits representing the fractional seconds.
     *
     * @return fractional seconds represented by this NTP timestamp.
     */
    public long getFraction() {
        return ntpTime & 0xffffffffL;
    }

    /**
     * Returns high-order 32-bits representing the seconds of this NTP timestamp.
     *
     * @return seconds represented by this NTP timestamp.
     */
    public long getSeconds() {
        return ntpTime >>> 32 & 0xffffffffL;
    }

    /**
     * Converts NTP timestamp to Java standard time.
     *
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this NTP timestamp value.
     */
    public long getTime() {
        return getTime(ntpTime);
    }

    /**
     * Computes a hash code for this Timestamp. The result is the exclusive OR of the two halves of the primitive <code>long</code> value represented by this
     * <code>TimeStamp</code> object. That is, the hash code is the value of the expression: <blockquote>
     *
     * <pre>
     * {@code
     * (int) (this.ntpValue() ^ (this.ntpValue() >>> 32))
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return (int) (ntpTime ^ ntpTime >>> 32);
    }

    /**
     * Returns the value of this Timestamp as a long value.
     *
     * @return the 64-bit long value represented by this object.
     */
    public long ntpValue() {
        return ntpTime;
    }

    private void readObject(final java.io.ObjectInputStream in) {
        throw new UnsupportedOperationException("Serialization is not supported");
    }

    /**
     * Converts this <code>TimeStamp</code> object to a <code>String</code> of the form: <blockquote>
     *
     * <pre>
     * EEE, MMM dd yyyy HH:mm:ss.SSS
     * </pre>
     *
     * </blockquote> See java.text.SimpleDataFormat for code descriptions.
     *
     * @return a string representation of this date.
     */
    public String toDateString() {
        if (simpleFormatter == null) {
            simpleFormatter = new SimpleDateFormat(NTP_DATE_FORMAT, Locale.US);
            simpleFormatter.setTimeZone(TimeZone.getDefault());
        }
        final Date ntpDate = getDate();
        return simpleFormatter.format(ntpDate);
    }

    /**
     * Converts this <code>TimeStamp</code> object to a <code>String</code>. The NTP timestamp 64-bit long value is represented as hex string with seconds
     * separated by fractional seconds by a decimal point; e.g. c1a089bd.fc904f6d == Tue, Dec 10 2002 10:41:49.986
     *
     * @return NTP timestamp 64-bit long value as hex string with seconds separated by fractional seconds.
     */
    @Override
    public String toString() {
        return toString(ntpTime);
    }

    /*
     * Serialization is unnecessary for this class. Reject attempts to do so until such time as the Serializable attribute can be dropped.
     */

    /**
     * Converts this <code>TimeStamp</code> object to a <code>String</code> of the form: <blockquote>
     *
     * <pre>
     * EEE, MMM dd yyyy HH:mm:ss.SSS UTC
     * </pre>
     *
     * </blockquote> See java.text.SimpleDataFormat for code descriptions.
     *
     * @return a string representation of this date in UTC.
     */
    public String toUTCString() {
        if (utcFormatter == null) {
            utcFormatter = new SimpleDateFormat(NTP_DATE_FORMAT + " 'UTC'", Locale.US);
            utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        final Date ntpDate = getDate();
        return utcFormatter.format(ntpDate);
    }

    private void writeObject(final java.io.ObjectOutputStream out) {
        throw new UnsupportedOperationException("Serialization is not supported");
    }

}
