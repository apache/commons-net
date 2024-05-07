package org.apache.commons.net.ftp.parser;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

/**
* Test NET710
*/
public class FTPTimestampNET710Test {

    @Test
    public void testNet710() throws ParseException {
        FTPTimestampParserImpl parser = new FTPTimestampParserImpl();
        Class<?> klass = FTPTimestampParserImpl.class; 
        System.out.println(klass.getResource('/' + klass.getName().replace('.', '/') + ".class"));
        Calendar serverTime = Calendar.getInstance(TimeZone.getTimeZone("EDT"), Locale.US);
        serverTime.set(2022, 2, 16, 14, 0);
        Calendar p = parser.parseTimestamp("Mar 13 02:33", serverTime);
        System.out.println(p);
    }

}
