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

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * Default implementation of the {@link  FTPTimestampParser  FTPTimestampParser}
 * interface also implements the {@link  org.apache.commons.net.ftp.Configurable  Configurable}
 * interface to allow the parsing to be configured from the outside.
 *
 * @see ConfigurableFTPFileEntryParserImpl
 * @since 1.4
 */
public class FTPTimestampParserImpl implements
        FTPTimestampParser, Configurable
{


    private SimpleDateFormat defaultDateFormat;
    private SimpleDateFormat recentDateFormat;
    private boolean lenientFutureDates = false;


    /**
     * The only constructor for this class.
     */
    public FTPTimestampParserImpl() {
        setDefaultDateFormat(DEFAULT_SDF);
        setRecentDateFormat(DEFAULT_RECENT_SDF);
    }

    /**
     * Implements the one {@link  FTPTimestampParser#parseTimestamp(String)  method}
     * in the {@link  FTPTimestampParser  FTPTimestampParser} interface
     * according to this algorithm:
     *
     * If the recentDateFormat member has been defined, try to parse the
     * supplied string with that.  If that parse fails, or if the recentDateFormat
     * member has not been defined, attempt to parse with the defaultDateFormat
     * member.  If that fails, throw a ParseException.
     *
     * This method assumes that the server time is the same as the local time.
     *
     * @see FTPTimestampParserImpl#parseTimestamp(String, Calendar)
     *
     * @param timestampStr The timestamp to be parsed
     */
//    @Override
    public Calendar parseTimestamp(String timestampStr) throws ParseException {
        Calendar now = Calendar.getInstance();
        return parseTimestamp(timestampStr, now);
    }

    /**
     * If the recentDateFormat member has been defined, try to parse the
     * supplied string with that.  If that parse fails, or if the recentDateFormat
     * member has not been defined, attempt to parse with the defaultDateFormat
     * member.  If that fails, throw a ParseException.
     *
     * This method allows a {@link Calendar} instance to be passed in which represents the
     * current (system) time.
     *
     * @see FTPTimestampParser#parseTimestamp(String)
     * @param timestampStr The timestamp to be parsed
     * @param serverTime The current time for the server
     * @since 1.5
     */
    public Calendar parseTimestamp(String timestampStr, Calendar serverTime) throws ParseException {
        Calendar working = (Calendar) serverTime.clone();
        working.setTimeZone(getServerTimeZone()); // is this needed?

        Date parsed = null;

        if (recentDateFormat != null) {
            Calendar now = (Calendar) serverTime.clone();// Copy this, because we may change it
            now.setTimeZone(this.getServerTimeZone());
            if (lenientFutureDates) {
                // add a day to "now" so that "slop" doesn't cause a date
                // slightly in the future to roll back a full year.  (Bug 35181 => NET-83)
                now.add(Calendar.DATE, 1);
            }
            // The Java SimpleDateFormat class uses the epoch year 1970 if not present in the input
            // As 1970 was not a leap year, it cannot parse "Feb 29" correctly.
            // Java 1.5+ returns Mar 1 1970
            // Temporarily add the current year to the short date time
            // to cope with short-date leap year strings.
            // Since Feb 29 is more that 6 months from the end of the year, this should be OK for
            // all instances of short dates which are +- 6 months from current date.
            // TODO this won't always work for systems that use short dates +0/-12months
            // e.g. if today is Jan 1 2001 and the short date is Feb 29
            String year = Integer.toString(now.get(Calendar.YEAR));
            String timeStampStrPlusYear = timestampStr + " " + year;
            SimpleDateFormat hackFormatter = new SimpleDateFormat(recentDateFormat.toPattern() + " yyyy",
                    recentDateFormat.getDateFormatSymbols());
            hackFormatter.setLenient(false);
            hackFormatter.setTimeZone(recentDateFormat.getTimeZone());
            ParsePosition pp = new ParsePosition(0);
            parsed = hackFormatter.parse(timeStampStrPlusYear, pp);
            // Check if we parsed the full string, if so it must have been a short date originally
            if (parsed != null && pp.getIndex() == timeStampStrPlusYear.length()) {
                working.setTime(parsed);
                if (working.after(now)) { // must have been last year instead
                    working.add(Calendar.YEAR, -1);
                }
                return working;
            }
        }

        ParsePosition pp = new ParsePosition(0);
        parsed = defaultDateFormat.parse(timestampStr, pp);
        // note, length checks are mandatory for us since
        // SimpleDateFormat methods will succeed if less than
        // full string is matched.  They will also accept,
        // despite "leniency" setting, a two-digit number as
        // a valid year (e.g. 22:04 will parse as 22 A.D.)
        // so could mistakenly confuse an hour with a year,
        // if we don't insist on full length parsing.
        if (parsed != null && pp.getIndex() == timestampStr.length()) {
            working.setTime(parsed);
        } else {
            throw new ParseException(
                    "Timestamp '"+timestampStr+"' could not be parsed using a server time of "
                        +serverTime.getTime().toString(),
                    pp.getErrorIndex());
        }
        return working;
    }

    /**
     * @return Returns the defaultDateFormat.
     */
    public SimpleDateFormat getDefaultDateFormat() {
        return defaultDateFormat;
    }
    /**
     * @return Returns the defaultDateFormat pattern string.
     */
    public String getDefaultDateFormatString() {
        return defaultDateFormat.toPattern();
    }
    /**
     * @param defaultDateFormat The defaultDateFormat to be set.
     */
    private void setDefaultDateFormat(String format) {
        if (format != null) {
            this.defaultDateFormat = new SimpleDateFormat(format);
            this.defaultDateFormat.setLenient(false);
        }
    }
    /**
     * @return Returns the recentDateFormat.
     */
    public SimpleDateFormat getRecentDateFormat() {
        return recentDateFormat;
    }
    /**
     * @return Returns the recentDateFormat.
     */
    public String getRecentDateFormatString() {
        return recentDateFormat.toPattern();
    }
    /**
     * @param recentDateFormat The recentDateFormat to set.
     */
    private void setRecentDateFormat(String format) {
        if (format != null) {
            this.recentDateFormat = new SimpleDateFormat(format);
            this.recentDateFormat.setLenient(false);
        }
    }

    /**
     * @return returns an array of 12 strings representing the short
     * month names used by this parse.
     */
    public String[] getShortMonths() {
        return defaultDateFormat.getDateFormatSymbols().getShortMonths();
    }


    /**
     * @return Returns the serverTimeZone used by this parser.
     */
    public TimeZone getServerTimeZone() {
        return this.defaultDateFormat.getTimeZone();
    }
    /**
     * sets a TimeZone represented by the supplied ID string into all
     * of the parsers used by this server.
     * @param serverTimeZone Time Id java.util.TimeZone id used by
     * the ftp server.  If null the client's local time zone is assumed.
     */
    private void setServerTimeZone(String serverTimeZoneId) {
        TimeZone serverTimeZone = TimeZone.getDefault();
        if (serverTimeZoneId != null) {
            serverTimeZone = TimeZone.getTimeZone(serverTimeZoneId);
        }
        this.defaultDateFormat.setTimeZone(serverTimeZone);
        if (this.recentDateFormat != null) {
            this.recentDateFormat.setTimeZone(serverTimeZone);
        }
    }

    /**
     * Implementation of the {@link  Configurable  Configurable}
     * interface. Configures this <code>FTPTimestampParser</code> according
     * to the following logic:
     * <p>
     * Set up the {@link  FTPClientConfig#setDefaultDateFormatStr(java.lang.String) defaultDateFormat}
     * and optionally the {@link  FTPClientConfig#setRecentDateFormatStr(String) recentDateFormat}
     * to values supplied in the config based on month names configured as follows:
     * </p><p><ul>
     * <li>If a {@link  FTPClientConfig#setShortMonthNames(String) shortMonthString}
     * has been supplied in the <code>config</code>, use that to parse  parse timestamps.</li>
     * <li>Otherwise, if a {@link  FTPClientConfig#setServerLanguageCode(String) serverLanguageCode}
     * has been supplied in the <code>config</code>, use the month names represented
     * by that {@link  FTPClientConfig#lookupDateFormatSymbols(String) language}
     * to parse timestamps.</li>
     * <li>otherwise use default English month names</li>
     * </ul></p><p>
     * Finally if a {@link  org.apache.commons.net.ftp.FTPClientConfig#setServerTimeZoneId(String) serverTimeZoneId}
     * has been supplied via the config, set that into all date formats that have
     * been configured.
     * </p>
     */
//    @Override
    public void configure(FTPClientConfig config) {
        DateFormatSymbols dfs = null;

        String languageCode = config.getServerLanguageCode();
        String shortmonths = config.getShortMonthNames();
        if (shortmonths != null) {
            dfs = FTPClientConfig.getDateFormatSymbols(shortmonths);
        } else if (languageCode != null) {
            dfs = FTPClientConfig.lookupDateFormatSymbols(languageCode);
        } else {
            dfs = FTPClientConfig.lookupDateFormatSymbols("en");
        }


        String recentFormatString = config.getRecentDateFormatStr();
        if (recentFormatString == null) {
            this.recentDateFormat = null;
        } else {
            this.recentDateFormat = new SimpleDateFormat(recentFormatString, dfs);
            this.recentDateFormat.setLenient(false);
        }

        String defaultFormatString = config.getDefaultDateFormatStr();
        if (defaultFormatString == null) {
            throw new IllegalArgumentException("defaultFormatString cannot be null");
        }
        this.defaultDateFormat = new SimpleDateFormat(defaultFormatString, dfs);
        this.defaultDateFormat.setLenient(false);

        setServerTimeZone(config.getServerTimeZoneId());

        this.lenientFutureDates = config.isLenientFutureDates();
    }
    /**
     * @return Returns the lenientFutureDates.
     */
    boolean isLenientFutureDates() {
        return lenientFutureDates;
    }
    /**
     * @param lenientFutureDates The lenientFutureDates to set.
     */
    void setLenientFutureDates(boolean lenientFutureDates) {
        this.lenientFutureDates = lenientFutureDates;
    }
}
