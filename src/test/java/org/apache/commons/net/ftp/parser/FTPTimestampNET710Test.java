package org.apache.commons.net.ftp.parser;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
* Test NET710
*/
public class FTPTimestampNET710Test {

    public static void main(String [] args) throws ParseException {
        Calendar serverTime = Calendar.getInstance(TimeZone.getTimeZone("EDT"), Locale.US);
        serverTime.set(2022, 2, 16, 14, 0);
        Calendar p = new FTPTimestampParserImpl().parseTimestamp("Mar 13 02:33", serverTime);
        System.out.println(p);
    }

}
